package com.yourdocs.data.local.dao

import androidx.room.*
import com.yourdocs.data.local.entity.DocumentEntity
import com.yourdocs.data.local.entity.DocumentWithFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE folder_id = :folderId ORDER BY created_at DESC")
    fun observeDocumentsInFolder(folderId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE folder_id = :folderId ORDER BY created_at DESC")
    suspend fun getDocumentsInFolder(folderId: String): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: String): DocumentEntity?

    @Query("SELECT COUNT(*) FROM documents WHERE folder_id = :folderId")
    suspend fun getDocumentCount(folderId: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: String)

    @Query("UPDATE documents SET folder_id = :newFolderId, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun moveDocument(documentId: String, newFolderId: String, updatedAt: Long)

    @Query("UPDATE documents SET original_name = :newName, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun updateDocumentName(documentId: String, newName: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getTotalDocumentCount(): Int

    @Query("SELECT * FROM documents")
    suspend fun getAllDocuments(): List<DocumentEntity>

    // Favorites
    @Query("SELECT d.*, f.name as folder_name FROM documents d JOIN folders f ON d.folder_id = f.id WHERE d.is_favorite = 1 ORDER BY d.updated_at DESC")
    fun observeFavorites(): Flow<List<DocumentWithFolder>>

    @Query("UPDATE documents SET is_favorite = :isFavorite, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun updateFavoriteStatus(documentId: String, isFavorite: Boolean, updatedAt: Long)

    // Recently Viewed
    @Query("SELECT d.*, f.name as folder_name FROM documents d JOIN folders f ON d.folder_id = f.id WHERE d.last_viewed_at IS NOT NULL ORDER BY d.last_viewed_at DESC LIMIT 10")
    fun observeRecentlyViewed(): Flow<List<DocumentWithFolder>>

    @Query("UPDATE documents SET last_viewed_at = :viewedAt WHERE id = :documentId")
    suspend fun updateLastViewedAt(documentId: String, viewedAt: Long)

    // Expiry
    @Query("SELECT d.*, f.name as folder_name FROM documents d JOIN folders f ON d.folder_id = f.id WHERE d.expiry_date IS NOT NULL AND d.expiry_date <= :thresholdMillis ORDER BY d.expiry_date ASC")
    fun observeExpiringSoon(thresholdMillis: Long): Flow<List<DocumentWithFolder>>

    @Query("UPDATE documents SET expiry_date = :expiryDate, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun updateExpiryDate(documentId: String, expiryDate: Long?, updatedAt: Long)

    @Query("SELECT * FROM documents WHERE expiry_date IS NOT NULL AND expiry_date > 0")
    suspend fun getDocumentsWithExpiry(): List<DocumentEntity>

    // Notes
    @Query("UPDATE documents SET notes = :notes, updated_at = :updatedAt WHERE id = :documentId")
    suspend fun updateNotes(documentId: String, notes: String?, updatedAt: Long)

    // Quick Search (cross-folder)
    @Query("SELECT d.*, f.name as folder_name FROM documents d JOIN folders f ON d.folder_id = f.id WHERE d.original_name LIKE '%' || :query || '%' OR f.name LIKE '%' || :query || '%' ORDER BY d.updated_at DESC")
    fun searchDocuments(query: String): Flow<List<DocumentWithFolder>>

    // Duplicate detection
    @Query("SELECT * FROM documents WHERE folder_id = :folderId AND original_name = :name AND size_bytes = :sizeBytes LIMIT 1")
    suspend fun findDuplicate(folderId: String, name: String, sizeBytes: Long): DocumentEntity?

    // Multi-select delete
    @Query("DELETE FROM documents WHERE id IN (:ids)")
    suspend fun deleteDocumentsByIds(ids: List<String>)

    // Multi-select move
    @Query("UPDATE documents SET folder_id = :newFolderId, updated_at = :updatedAt WHERE id IN (:ids)")
    suspend fun moveDocuments(ids: List<String>, newFolderId: String, updatedAt: Long)
}
