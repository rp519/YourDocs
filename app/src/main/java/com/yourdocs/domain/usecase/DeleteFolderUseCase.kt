package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

/**
 * Use case to delete a folder and all its documents.
 * This is a destructive operation that should be confirmed by the user.
 */
class DeleteFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String): Result<Unit> {
        return folderRepository.deleteFolder(folderId)
    }
}
