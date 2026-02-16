package com.yourdocs.data.local.dao

import androidx.room.*
import com.yourdocs.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for document operations.
 */
@Dao
interface DocumentDao {

    /**
     * Get all documents in a folder, ordered by most recent first.
     */
    @Query("SELECT * FROM documents WHERE folder_id = :folderId ORDER BY created_at DESC")
    fun observeDocumentsInFolder(folderId: String): Flow<List<DocumentEntity>>

    /**
     * Get all documents in a folder (single shot).
     */
    @Query("SELECT * FROM documents WHERE folder_id = :folderId ORDER BY created_at DESC")
    suspend fun getDocumentsInFolder(folderId: String): List<DocumentEntity>

    /**
     * Get a document by ID.
     */
    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: String): DocumentEntity?

    /**
     * Get document count for a folder.
     */
    @Query("SELECT COUNT(*) FROM documents WHERE folder_id = :folderId")
    suspend fun getDocumentCount(folderId: String): Int

    /**
     * Insert a new document.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocument(document: DocumentEntity)

    /**
     * Update an existing document.
     */
    @Update
    suspend fun updateDocument(document: DocumentEntity)

    /**
     * Delete a document.
     */
    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    /**
     * Delete a document by ID.
     */
    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: String)

    /**
     * Move a document to a different folder.
     */
    @Query("UPDATE documents SET folder_id = :newFolderId, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun moveDocument(documentId: String, newFolderId: String, updatedAt: Long)

    /**
     * Update document name.
     */
    @Query("UPDATE documents SET original_name = :newName, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun updateDocumentName(documentId: String, newName: String, updatedAt: Long)

    /**
     * Get all documents (for export/migration).
     */
    @Query("SELECT * FROM documents")
    suspend fun getAllDocuments(): List<DocumentEntity>
}
