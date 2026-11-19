package com.medsdate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for image handling.
 */
object ImageUtils {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    fun copyImageToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            // First pass: get original dimensions without loading pixels
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOptions)
            }

            // Calculate power-of-2 subsample to stay near MAX_DIMENSION
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(boundsOptions)
            }

            // Second pass: decode subsampled bitmap
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            } ?: return null

            // Re-encoding drops EXIF, so apply the camera orientation to the pixels first
            val orientation = context.contentResolver.openInputStream(sourceUri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
            val oriented = orientationMatrix(orientation)?.let {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, it, true)
            } ?: bitmap

            // Write JPEG-compressed bytes to internal storage
            val imageFile = createImageFile(context)
            FileOutputStream(imageFile).use { outputStream ->
                oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }
            if (oriented !== bitmap) oriented.recycle()
            bitmap.recycle()

            imageFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy image from URI: $sourceUri")
            null
        }
    }

    private fun orientationMatrix(orientation: Int): Matrix? = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { postRotate(90f) }
        ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { postRotate(180f) }
        ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { postRotate(270f) }
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { postScale(1f, -1f) }
        ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply { postRotate(90f); postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply { postRotate(270f); postScale(-1f, 1f) }
        else -> null
    }

    /**
     * Creates an empty file in cache/camera and returns its FileProvider URI, as the target of
     * the system camera (TakePicture). Delete it through the content resolver after use.
     */
    fun createCameraTempUri(context: Context): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File.createTempFile("IMG_", ".jpg", dir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > MAX_DIMENSION || width > MAX_DIMENSION) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= MAX_DIMENSION && halfWidth / inSampleSize >= MAX_DIMENSION) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Creates a unique file for storing a medicine image in internal storage.
     *
     * @param context The application context
     * @return The created file
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "MEDICINE_$timeStamp"
        val storageDir = File(context.filesDir, "images")

        // Create directory if it doesn't exist
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(storageDir, "$imageFileName.jpg")
    }

    /**
     * Deletes an image file from app storage.
     *
     * @param imagePath The absolute path to the image file
     * @return true if deletion was successful, false otherwise
     */
    fun deleteImage(imagePath: String?): Boolean {
        if (imagePath.isNullOrBlank()) return false

        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete image: $imagePath")
            false
        }
    }
}
