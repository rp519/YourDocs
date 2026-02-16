package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(
        name: String,
        colorHex: String? = null,
        emoji: String? = null
    ): Result<Folder> {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Folder name cannot be empty"))
        }

        if (trimmedName.length > MAX_FOLDER_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Folder name too long (max $MAX_FOLDER_NAME_LENGTH characters)")
            )
        }

        return folderRepository.createFolder(trimmedName, colorHex, emoji)
    }

    companion object {
        private const val MAX_FOLDER_NAME_LENGTH = 100
    }
}
