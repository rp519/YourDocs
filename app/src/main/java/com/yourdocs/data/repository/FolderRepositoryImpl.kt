package com.yourdocs.data.repository

import com.yourdocs.data.local.dao.FolderDao
import com.yourdocs.data.local.entity.FolderEntity
import com.yourdocs.data.local.entity.toDomain
import com.yourdocs.data.local.entity.toEntity
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FolderRepository using Room database.
 */
@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun observeAllFolders(): Flow<List<Folder>> {
        return folderDao.observeAllFoldersWithCount()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAllFolders(): List<Folder> {
        return folderDao.getAllFoldersWithCount().map { it.toDomain() }
    }

    override suspend fun getFolderById(folderId: String): Folder? {
        val entity = folderDao.getFolderById(folderId) ?: return null
        // Get document count separately
        return entity.toDomain(documentCount = 0) // Will be updated by DAO query if needed
    }

    override suspend fun createFolder(name: String, colorHex: String?, emoji: String?): Result<Folder> {
        return try {
            // Check if name already exists
            if (folderDao.folderNameExists(name)) {
                return Result.failure(IllegalArgumentException("Folder name already exists"))
            }

            val now = Instant.now()
            val folder = Folder(
                id = Folder.generateId(),
                name = name.trim(),
                isPinned = false,
                isLocked = false,
                documentCount = 0,
                colorHex = colorHex,
                emoji = emoji,
                createdAt = now,
                updatedAt = now
            )

            folderDao.insertFolder(folder.toEntity())
            Result.success(folder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameFolder(folderId: String, newName: String): Result<Unit> {
        return try {
            val trimmedName = newName.trim()

            // Check if folder exists
            val folder = folderDao.getFolderById(folderId)
                ?: return Result.failure(IllegalArgumentException("Folder not found"))

            // Check if new name already exists (excluding current folder)
            if (folderDao.folderNameExists(trimmedName, excludeId = folderId)) {
                return Result.failure(IllegalArgumentException("Folder name already exists"))
            }

            val now = Instant.now().toEpochMilli()
            folderDao.updateFolderName(folderId, trimmedName, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFolder(folderId: String): Result<Unit> {
        return try {
            folderDao.deleteFolderById(folderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePinned(folderId: String): Result<Unit> {
        return try {
            val folder = folderDao.getFolderById(folderId)
                ?: return Result.failure(IllegalArgumentException("Folder not found"))

            val now = Instant.now().toEpochMilli()
            folderDao.updatePinnedStatus(folderId, !folder.isPinned, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setLocked(folderId: String, isLocked: Boolean): Result<Unit> {
        return try {
            val folder = folderDao.getFolderById(folderId)
                ?: return Result.failure(IllegalArgumentException("Folder not found"))

            val now = Instant.now().toEpochMilli()
            folderDao.updateLockedStatus(folderId, isLocked, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAppearance(folderId: String, colorHex: String?, emoji: String?): Result<Unit> {
        return try {
            val now = Instant.now().toEpochMilli()
            folderDao.updateFolderAppearance(folderId, colorHex, emoji, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
