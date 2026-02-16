package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

/**
 * Use case to rename a folder.
 * Validates that new name is not empty and doesn't already exist.
 */
class RenameFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String, newName: String): Result<Unit> {
        val trimmedName = newName.trim()

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Folder name cannot be empty"))
        }

        if (trimmedName.length > MAX_FOLDER_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Folder name too long (max $MAX_FOLDER_NAME_LENGTH characters)")
            )
        }

        return folderRepository.renameFolder(folderId, trimmedName)
    }

    companion object {
        private const val MAX_FOLDER_NAME_LENGTH = 100
    }
}
