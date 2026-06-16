package com.medsdate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

            // Write JPEG-compressed bytes to internal storage
            val imageFile = createImageFile(context)
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }
            bitmap.recycle()

            imageFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy image from URI: $sourceUri")
            null
        }
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

    /**
     * Gets a content URI for a file using FileProvider.
     *
     * @param context The application context
     * @param file The file to get URI for
     * @return The content URI
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
