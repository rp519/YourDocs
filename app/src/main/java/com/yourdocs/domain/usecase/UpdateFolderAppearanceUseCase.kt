package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class UpdateFolderAppearanceUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String, colorHex: String?, emoji: String?): Result<Unit> {
        return folderRepository.updateAppearance(folderId, colorHex, emoji)
    }
}
