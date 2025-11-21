package com.example.cloudcareapp.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * File Downloader Utility
 * Handles downloading and opening base64-encoded files
 */
object FileDownloader {
    
    /**
     * Download and open a base64-encoded file
     * 
     * @param context Android context
     * @param base64Data Base64-encoded file data
     * @param fileName Name for the downloaded file
     * @param mimeType MIME type of the file (e.g., "application/pdf", "image/jpeg")
     * @return true if successful, false otherwise
     */
    fun downloadAndOpenBase64File(
        context: Context,
        base64Data: String,
        fileName: String,
        mimeType: String = "application/pdf"
    ): Boolean {
        return try {
            // Remove data URI prefix if present (e.g., "data:application/pdf;base64,")
            val cleanBase64 = base64Data.substringAfter("base64,").trim()
            
            // Decode base64 to bytes
            val fileBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            
            // Create file in app's cache directory
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(fileBytes)
            }
            
            // Get URI using FileProvider for Android 7.0+
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Create intent to open file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Try to open the file
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to open file: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
    
    /**
     * Save base64-encoded file to Downloads folder
     * 
     * @param context Android context
     * @param base64Data Base64-encoded file data
     * @param fileName Name for the downloaded file
     * @return true if successful, false otherwise
     */
    fun saveBase64ToDownloads(
        context: Context,
        base64Data: String,
        fileName: String
    ): Boolean {
        return try {
            // Remove data URI prefix if present
            val cleanBase64 = base64Data.substringAfter("base64,").trim()
            
            // Decode base64 to bytes
            val fileBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            
            // Create file in Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(fileBytes)
            }
            
            Toast.makeText(
                context,
                "File saved to Downloads: $fileName",
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to save file: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
    
    /**
     * Determine MIME type from file extension
     */
    fun getMimeTypeFromExtension(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Generate filename from record title and type
     */
    fun generateFileName(title: String, recordType: String): String {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_\\- ]"), "").take(50)
        val timestamp = System.currentTimeMillis()
        val extension = when (recordType.uppercase()) {
            "LAB_REPORT", "PRESCRIPTION", "DISCHARGE_SUMMARY" -> "pdf"
            "X_RAY", "CT_SCAN", "MRI", "ULTRASOUND" -> "jpg"
            else -> "pdf"
        }
        return "${sanitizedTitle}_${timestamp}.${extension}"
    }
}
