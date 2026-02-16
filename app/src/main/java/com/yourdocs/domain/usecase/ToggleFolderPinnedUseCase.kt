package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

/**
 * Use case to toggle a folder's pinned status.
 * Pinned folders appear first in the folder list.
 */
class ToggleFolderPinnedUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String): Result<Unit> {
        return folderRepository.togglePinned(folderId)
    }
}
