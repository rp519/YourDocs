package com.yourdocs.domain.repository

import android.net.Uri
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for document operations.
 * Defines the contract for document data access.
 */
interface DocumentRepository {

    /**
     * Observe all documents in a folder, ordered by most recent first.
     */
    fun observeDocumentsInFolder(folderId: String): Flow<List<Document>>

    /**
     * Get a document by ID.
     */
    suspend fun getDocumentById(documentId: String): Document?

    /**
     * Import a document from a content URI.
     * Resolves metadata via ContentResolver, copies the file, and inserts a DB record.
     *
     * @param overrideName If provided, used as the display name instead of resolving from URI.
     * @param pageCount If provided, stored as the document's page count (e.g., for scanned PDFs).
     */
    suspend fun importDocument(
        folderId: String,
        uri: Uri,
        source: DocumentSource,
        overrideName: String? = null,
        pageCount: Int? = null
    ): Result<Document>

    /**
     * Delete a document (physical file + DB record).
     */
    suspend fun deleteDocument(documentId: String): Result<Unit>

    /**
     * Move a document to a different folder.
     */
    suspend fun moveDocument(documentId: String, newFolderId: String): Result<Unit>

    /**
     * Rename a document.
     */
    suspend fun renameDocument(documentId: String, newName: String): Result<Unit>
}
