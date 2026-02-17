package com.yourdocs.domain.repository

import android.net.Uri
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.model.UriMetadata
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    fun observeDocumentsInFolder(folderId: String): Flow<List<Document>>

    suspend fun getDocumentById(documentId: String): Document?

    suspend fun importDocument(
        folderId: String,
        uri: Uri,
        source: DocumentSource,
        overrideName: String? = null,
        pageCount: Int? = null
    ): Result<Document>

    suspend fun deleteDocument(documentId: String): Result<Unit>

    suspend fun moveDocument(documentId: String, newFolderId: String): Result<Unit>

    suspend fun renameDocument(documentId: String, newName: String): Result<Unit>

    // Favorites
    fun observeFavorites(): Flow<List<DocumentWithFolderInfo>>
    suspend fun toggleFavorite(documentId: String): Result<Unit>

    // Recently viewed
    fun observeRecentlyViewed(): Flow<List<DocumentWithFolderInfo>>
    suspend fun markAsViewed(documentId: String): Result<Unit>

    // Expiry
    fun observeExpiringSoon(): Flow<List<DocumentWithFolderInfo>>
    suspend fun setExpiryDate(documentId: String, expiryDate: java.time.Instant?): Result<Unit>
    suspend fun getDocumentsWithExpiry(): List<Document>

    // Notes
    suspend fun updateNotes(documentId: String, notes: String?): Result<Unit>

    // Quick search
    fun searchDocuments(query: String): Flow<List<DocumentWithFolderInfo>>

    // Duplicate detection
    suspend fun findDuplicate(folderId: String, name: String, sizeBytes: Long): Document?

    // Multi-select
    suspend fun deleteDocuments(ids: List<String>): Result<Unit>
    suspend fun moveDocuments(ids: List<String>, newFolderId: String): Result<Unit>

    // URI metadata resolution
    suspend fun resolveUriMetadata(uri: Uri): UriMetadata
}
