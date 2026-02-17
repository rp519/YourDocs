package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.model.FreeLimitReachedException
import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository,
    private val checkFreeLimitsUseCase: CheckFreeLimitsUseCase
) {
    suspend operator fun invoke(
        name: String,
        colorHex: String? = null,
        emoji: String? = null,
        description: String? = null
    ): Result<Folder> {
        val limits = checkFreeLimitsUseCase()
        if (!limits.canCreateFolder) {
            return Result.failure(FreeLimitReachedException("folder"))
        }

        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Folder name cannot be empty"))
        }

        if (trimmedName.length > MAX_FOLDER_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Folder name too long (max $MAX_FOLDER_NAME_LENGTH characters)")
            )
        }

        return folderRepository.createFolder(trimmedName, colorHex, emoji, description)
    }

    companion object {
        private const val MAX_FOLDER_NAME_LENGTH = 100
    }
}
