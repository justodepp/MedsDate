package com.medsdate.utils

import android.content.Context
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

    /**
     * Copies an image from a URI (e.g., gallery selection) to app's internal storage.
     *
     * @param context The application context
     * @param sourceUri The URI of the source image
     * @return The file path of the copied image, or null if copy failed
     */
    fun copyImageToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            // Create destination file
            val imageFile = createImageFile(context)

            // Open input stream from source URI
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                // Open output stream to destination file
                FileOutputStream(imageFile).use { outputStream ->
                    // Copy data
                    inputStream.copyTo(outputStream)
                }
            }

            // Return the absolute path
            imageFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy image from URI: $sourceUri")
            null
        }
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
