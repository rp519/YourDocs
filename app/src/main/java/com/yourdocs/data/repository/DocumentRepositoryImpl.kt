package com.yourdocs.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.yourdocs.data.local.dao.DocumentDao
import com.yourdocs.data.local.entity.toDomain
import com.yourdocs.data.local.entity.toEntity
import com.yourdocs.data.local.storage.FileStorageManager
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val fileStorageManager: FileStorageManager,
    private val contentResolver: ContentResolver
) : DocumentRepository {

    override fun observeDocumentsInFolder(folderId: String): Flow<List<Document>> {
        return documentDao.observeDocumentsInFolder(folderId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDocumentById(documentId: String): Document? {
        return documentDao.getDocumentById(documentId)?.toDomain()
    }

    override suspend fun importDocument(
        folderId: String,
        uri: Uri,
        source: DocumentSource,
        overrideName: String?,
        pageCount: Int?
    ): Result<Document> = withContext(Dispatchers.IO) {
        try {
            // Resolve metadata from URI
            val (resolvedName, sizeBytes, mimeType) = resolveUriMetadata(uri)
            val displayName = overrideName ?: resolvedName

            // Generate unique stored filename
            val storedFileName = fileStorageManager.generateUniqueFileName(displayName)

            // Copy file to internal storage
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(
                    IllegalStateException("Cannot open input stream for URI")
                )

            fileStorageManager.saveDocument(inputStream, storedFileName)
                .onFailure { return@withContext Result.failure(it) }

            // Get actual file size after copy (more reliable for file:// URIs)
            val actualSize = if (sizeBytes > 0) sizeBytes
                else fileStorageManager.getDocumentSize(storedFileName)

            // Create domain model
            val now = Instant.now()
            val document = Document(
                id = Document.generateId(),
                folderId = folderId,
                originalName = displayName,
                storedFileName = storedFileName,
                mimeType = if (overrideName != null && mimeType == "application/octet-stream")
                    guessMimeType(displayName) else mimeType,
                sizeBytes = actualSize,
                source = source,
                pageCount = pageCount,
                createdAt = now,
                updatedAt = now
            )

            // Insert into DB
            documentDao.insertDocument(document.toEntity())
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }

    override suspend fun deleteDocument(documentId: String): Result<Unit> {
        return try {
            val entity = documentDao.getDocumentById(documentId)
                ?: return Result.failure(IllegalArgumentException("Document not found"))

            // Delete physical file first
            fileStorageManager.deleteDocument(entity.storedFileName)

            // Then delete DB record
            documentDao.deleteDocumentById(documentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveDocument(documentId: String, newFolderId: String): Result<Unit> {
        return try {
            val entity = documentDao.getDocumentById(documentId)
                ?: return Result.failure(IllegalArgumentException("Document not found"))

            val now = Instant.now().toEpochMilli()
            documentDao.moveDocument(documentId, newFolderId, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameDocument(documentId: String, newName: String): Result<Unit> {
        return try {
            val entity = documentDao.getDocumentById(documentId)
                ?: return Result.failure(IllegalArgumentException("Document not found"))

            val now = Instant.now().toEpochMilli()
            documentDao.updateDocumentName(documentId, newName.trim(), now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resolveUriMetadata(uri: Uri): UriMetadata {
        var displayName = "unknown"
        var sizeBytes = 0L
        var mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex) ?: "unknown"
                }
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        return UriMetadata(displayName, sizeBytes, mimeType)
    }

    private data class UriMetadata(
        val displayName: String,
        val sizeBytes: Long,
        val mimeType: String
    )
}
