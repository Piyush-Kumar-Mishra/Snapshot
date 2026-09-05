package com.example.snapshot.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    /**
     * Saves the generated collage bitmap into the device's public Pictures/SnapshotCollages gallery folder.
     */
    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filename: String = "Collage_${System.currentTimeMillis()}.png"
    ): Uri? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SnapshotCollages")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext null

        try {
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }

            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a temporary copy of the collage in the cache directory and creates an ACTION_SEND Intent
     * with FileProvider content URI to trigger the standard Android Share Sheet.
     */
    suspend fun createShareIntent(context: Context, bitmap: Bitmap): Intent = withContext(Dispatchers.IO) {
        val cacheFolder = File(context.cacheDir, "shared_collages").apply { mkdirs() }
        val shareFile = File(cacheFolder, "snapshot_collage_${System.currentTimeMillis()}.png")

        FileOutputStream(shareFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile
        )

        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Snapshot - Unique People Collage")
            putExtra(Intent.EXTRA_TEXT, "Generated with Snapshot on-device AI!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
