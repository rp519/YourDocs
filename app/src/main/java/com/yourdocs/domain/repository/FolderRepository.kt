package com.yourdocs.domain.repository

import com.yourdocs.domain.model.Folder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for folder operations.
 * Defines the contract for folder data access.
 */
interface FolderRepository {

    /**
     * Observe all folders with document counts.
     * Folders are sorted: pinned first, then alphabetically.
     */
    fun observeAllFolders(): Flow<List<Folder>>

    /**
     * Get all folders (single shot).
     */
    suspend fun getAllFolders(): List<Folder>

    /**
     * Get a folder by ID.
     */
    suspend fun getFolderById(folderId: String): Folder?

    /**
     * Create a new folder.
     * Returns the created folder or null if name already exists.
     */
    suspend fun createFolder(name: String, colorHex: String? = null, emoji: String? = null, description: String? = null): Result<Folder>

    /**
     * Rename a folder.
     * Returns success or error if name already exists or folder not found.
     */
    suspend fun renameFolder(folderId: String, newName: String): Result<Unit>

    /**
     * Delete a folder and all its documents.
     */
    suspend fun deleteFolder(folderId: String): Result<Unit>

    /**
     * Toggle folder pinned status.
     */
    suspend fun togglePinned(folderId: String): Result<Unit>

    /**
     * Set folder locked status with optional lock method.
     */
    suspend fun setLocked(folderId: String, isLocked: Boolean, lockMethod: String? = null): Result<Unit>

    /**
     * Update folder appearance (color and emoji).
     */
    suspend fun updateAppearance(folderId: String, colorHex: String?, emoji: String?): Result<Unit>

    /**
     * Update folder description.
     */
    suspend fun updateDescription(folderId: String, description: String?): Result<Unit>
}
