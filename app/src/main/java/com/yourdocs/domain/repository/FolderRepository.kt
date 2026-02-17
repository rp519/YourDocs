package com.yourdocs.domain.repository

import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.model.SortPreference
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun observeAllFolders(): Flow<List<Folder>>

    suspend fun getAllFolders(): List<Folder>

    suspend fun getFolderById(folderId: String): Folder?

    suspend fun createFolder(name: String, colorHex: String? = null, emoji: String? = null, description: String? = null): Result<Folder>

    suspend fun renameFolder(folderId: String, newName: String): Result<Unit>

    suspend fun deleteFolder(folderId: String): Result<Unit>

    suspend fun togglePinned(folderId: String): Result<Unit>

    suspend fun setLocked(folderId: String, isLocked: Boolean, lockMethod: String? = null): Result<Unit>

    suspend fun updateAppearance(folderId: String, colorHex: String?, emoji: String?): Result<Unit>

    suspend fun updateDescription(folderId: String, description: String?): Result<Unit>

    suspend fun setSortPreference(folderId: String, sort: SortPreference): Result<Unit>
}
