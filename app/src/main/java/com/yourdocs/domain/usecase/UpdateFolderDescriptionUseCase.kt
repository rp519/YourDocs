package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class UpdateFolderDescriptionUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String, description: String?): Result<Unit> {
        return folderRepository.updateDescription(folderId, description?.trim()?.ifEmpty { null })
    }
}
