package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class ToggleFolderLockedUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String): Result<Unit> {
        val folder = folderRepository.getFolderById(folderId)
            ?: return Result.failure(IllegalArgumentException("Folder not found"))
        return folderRepository.setLocked(folderId, !folder.isLocked)
    }
}
