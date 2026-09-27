package com.cashmath.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileStorageHelper {

    fun saveAttachmentLocally(context: Context, sourceUri: Uri): String? {
        return saveSingleFileLocally(context, sourceUri)
    }

    fun saveSingleFileLocally(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val receiptsDir = File(context.filesDir, "receipts").apply {
                if (!exists()) mkdirs()
            }

            val mimeType = contentResolver.getType(sourceUri)
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "application/pdf" -> "pdf"
                else -> "jpg"
            }

            val targetFile = File(receiptsDir, "receipt_${UUID.randomUUID()}.$extension")

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveMultipleFilesLocally(context: Context, uris: List<Uri>): List<String> {
        return uris.mapNotNull { saveSingleFileLocally(context, it) }
    }

    fun deleteLocalFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
