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
import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.model.UriMetadata
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
            val metadata = resolveUriMetadata(uri)
            val displayName = overrideName ?: metadata.displayName

            val storedFileName = fileStorageManager.generateUniqueFileName(displayName)

            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(
                    IllegalStateException("Cannot open input stream for URI")
                )

            fileStorageManager.saveDocument(inputStream, storedFileName)
                .onFailure { return@withContext Result.failure(it) }

            val actualSize = if (metadata.sizeBytes > 0) metadata.sizeBytes
                else fileStorageManager.getDocumentSize(storedFileName)

            val now = Instant.now()
            val document = Document(
                id = Document.generateId(),
                folderId = folderId,
                originalName = displayName,
                storedFileName = storedFileName,
                mimeType = if (overrideName != null && metadata.mimeType == "application/octet-stream")
                    guessMimeType(displayName) else metadata.mimeType,
                sizeBytes = actualSize,
                source = source,
                pageCount = pageCount,
                createdAt = now,
                updatedAt = now
            )

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
            fileStorageManager.deleteDocument(entity.storedFileName)
            documentDao.deleteDocumentById(documentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveDocument(documentId: String, newFolderId: String): Result<Unit> {
        return try {
            documentDao.getDocumentById(documentId)
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
            documentDao.getDocumentById(documentId)
                ?: return Result.failure(IllegalArgumentException("Document not found"))
            val now = Instant.now().toEpochMilli()
            documentDao.updateDocumentName(documentId, newName.trim(), now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Favorites

    override fun observeFavorites(): Flow<List<DocumentWithFolderInfo>> {
        return documentDao.observeFavorites()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun toggleFavorite(documentId: String): Result<Unit> {
        return try {
            val entity = documentDao.getDocumentById(documentId)
                ?: return Result.failure(IllegalArgumentException("Document not found"))
            val now = Instant.now().toEpochMilli()
            documentDao.updateFavoriteStatus(documentId, !entity.isFavorite, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Recently viewed

    override fun observeRecentlyViewed(): Flow<List<DocumentWithFolderInfo>> {
        return documentDao.observeRecentlyViewed()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun markAsViewed(documentId: String): Result<Unit> {
        return try {
            val now = Instant.now().toEpochMilli()
            documentDao.updateLastViewedAt(documentId, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Expiry

    override fun observeExpiringSoon(): Flow<List<DocumentWithFolderInfo>> {
        val thresholdMillis = Instant.now().plusSeconds(30L * 24 * 60 * 60).toEpochMilli()
        return documentDao.observeExpiringSoon(thresholdMillis)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun setExpiryDate(documentId: String, expiryDate: Instant?): Result<Unit> {
        return try {
            val now = Instant.now().toEpochMilli()
            documentDao.updateExpiryDate(documentId, expiryDate?.toEpochMilli(), now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDocumentsWithExpiry(): List<Document> {
        return documentDao.getDocumentsWithExpiry().map { it.toDomain() }
    }

    // Notes

    override suspend fun updateNotes(documentId: String, notes: String?): Result<Unit> {
        return try {
            val now = Instant.now().toEpochMilli()
            documentDao.updateNotes(documentId, notes, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Quick search

    override fun searchDocuments(query: String): Flow<List<DocumentWithFolderInfo>> {
        return documentDao.searchDocuments(query)
            .map { list -> list.map { it.toDomain() } }
    }

    // Duplicate detection

    override suspend fun findDuplicate(folderId: String, name: String, sizeBytes: Long): Document? {
        return documentDao.findDuplicate(folderId, name, sizeBytes)?.toDomain()
    }

    // Multi-select

    override suspend fun deleteDocuments(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { id ->
                documentDao.getDocumentById(id)?.let { entity ->
                    fileStorageManager.deleteDocument(entity.storedFileName)
                }
            }
            documentDao.deleteDocumentsByIds(ids)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveDocuments(ids: List<String>, newFolderId: String): Result<Unit> {
        return try {
            val now = Instant.now().toEpochMilli()
            documentDao.moveDocuments(ids, newFolderId, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // URI metadata resolution

    override suspend fun resolveUriMetadata(uri: Uri): UriMetadata {
        var displayName = "unknown"
        var sizeBytes = 0L
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

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
}
