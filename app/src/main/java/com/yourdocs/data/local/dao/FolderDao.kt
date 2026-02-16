package com.yourdocs.data.local.dao

import androidx.room.*
import com.yourdocs.data.local.entity.FolderEntity
import com.yourdocs.data.local.entity.FolderWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for folder operations.
 */
@Dao
interface FolderDao {

    /**
     * Get all folders with document counts, ordered by pinned status (pinned first) and then by name.
     */
    @Query("""
        SELECT f.*, COALESCE(COUNT(d.id), 0) as document_count
        FROM folders f
        LEFT JOIN documents d ON f.id = d.folder_id
        GROUP BY f.id
        ORDER BY f.is_pinned DESC, f.name ASC
    """)
    fun observeAllFoldersWithCount(): Flow<List<FolderWithCount>>

    /**
     * Get all folders with document counts (single shot).
     */
    @Query("""
        SELECT f.*, COALESCE(COUNT(d.id), 0) as document_count
        FROM folders f
        LEFT JOIN documents d ON f.id = d.folder_id
        GROUP BY f.id
        ORDER BY f.is_pinned DESC, f.name ASC
    """)
    suspend fun getAllFoldersWithCount(): List<FolderWithCount>

    /**
     * Get a folder by ID.
     */
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): FolderEntity?

    /**
     * Get a folder by ID as Flow.
     */
    @Query("SELECT * FROM folders WHERE id = :folderId")
    fun observeFolderById(folderId: String): Flow<FolderEntity?>

    /**
     * Check if a folder name already exists (case-insensitive).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM folders WHERE LOWER(name) = LOWER(:name) AND id != :excludeId)")
    suspend fun folderNameExists(name: String, excludeId: String = ""): Boolean

    /**
     * Insert a new folder.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolder(folder: FolderEntity)

    /**
     * Update an existing folder.
     */
    @Update
    suspend fun updateFolder(folder: FolderEntity)

    /**
     * Delete a folder (documents will be cascade deleted).
     */
    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    /**
     * Delete a folder by ID.
     */
    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: String)

    /**
     * Update folder's pinned status.
     */
    @Query("UPDATE folders SET is_pinned = :isPinned, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun updatePinnedStatus(folderId: String, isPinned: Boolean, updatedAt: Long)

    /**
     * Update folder's locked status.
     */
    @Query("UPDATE folders SET is_locked = :isLocked, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun updateLockedStatus(folderId: String, isLocked: Boolean, updatedAt: Long)

    /**
     * Update folder name.
     */
    @Query("UPDATE folders SET name = :name, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun updateFolderName(folderId: String, name: String, updatedAt: Long)

    /**
     * Update folder appearance (color and emoji).
     */
    @Query("UPDATE folders SET color_hex = :colorHex, emoji = :emoji, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun updateFolderAppearance(folderId: String, colorHex: String?, emoji: String?, updatedAt: Long)
}
