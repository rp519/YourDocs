package com.yourdocs.data.local.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages file storage operations for YourDocs.
 *
 * All documents are stored in a dedicated app folder structure:
 * /data/data/com.yourdocs/files/YourDocs/
 *   ├── documents/       # All document files stored here
 *   └── metadata.json    # Metadata export file (for migration)
 *
 * Files are stored with unique names to avoid conflicts.
 */
@Singleton
class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appFilesDir: File = context.filesDir
    private val yourDocsDir: File = File(appFilesDir, "YourDocs")
    private val documentsDir: File = File(yourDocsDir, "documents")

    /**
     * Initialize storage directories.
     * Called on app startup to ensure directories exist.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (!yourDocsDir.exists()) {
            yourDocsDir.mkdirs()
        }
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
    }

    /**
     * Get the YourDocs root directory path.
     * This is what users should back up for migration.
     */
    fun getYourDocsDirectoryPath(): String = yourDocsDir.absolutePath

    /**
     * Generate a unique filename for storing a document.
     * Format: {timestamp}_{uuid}.{extension}
     */
    fun generateUniqueFileName(originalName: String): String {
        val extension = originalName.substringAfterLast('.', "")
        val timestamp = System.currentTimeMillis()
        val uuid = java.util.UUID.randomUUID().toString().take(8)
        return if (extension.isNotEmpty()) {
            "${timestamp}_${uuid}.$extension"
        } else {
            "${timestamp}_${uuid}"
        }
    }

    /**
     * Save a document from an input stream.
     * Returns the stored file name on success.
     */
    suspend fun saveDocument(
        inputStream: InputStream,
        storedFileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(documentsDir, storedFileName)
            inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Result.success(storedFileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save a document from a file.
     * Returns the stored file name on success.
     */
    suspend fun saveDocument(
        sourceFile: File,
        storedFileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(documentsDir, storedFileName)
            sourceFile.copyTo(targetFile, overwrite = false)
            Result.success(storedFileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a document file by its stored name.
     */
    fun getDocumentFile(storedFileName: String): File {
        return File(documentsDir, storedFileName)
    }

    /**
     * Check if a document file exists.
     */
    suspend fun documentExists(storedFileName: String): Boolean = withContext(Dispatchers.IO) {
        getDocumentFile(storedFileName).exists()
    }

    /**
     * Delete a document file.
     */
    suspend fun deleteDocument(storedFileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getDocumentFile(storedFileName)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the size of a document in bytes.
     */
    suspend fun getDocumentSize(storedFileName: String): Long = withContext(Dispatchers.IO) {
        try {
            getDocumentFile(storedFileName).length()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get metadata file for export.
     */
    fun getMetadataFile(): File {
        return File(yourDocsDir, "metadata.json")
    }

    /**
     * Get all document files in storage.
     * Useful for reconciliation and cleanup.
     */
    suspend fun getAllDocumentFiles(): List<File> = withContext(Dispatchers.IO) {
        documentsDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * Calculate total storage used by documents.
     */
    suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        getAllDocumentFiles().sumOf { it.length() }
    }

    /**
     * Get available storage space.
     */
    fun getAvailableStorage(): Long {
        return yourDocsDir.usableSpace
    }
}
