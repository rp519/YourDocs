package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.SortPreference
import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class SetFolderSortUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String, sort: SortPreference): Result<Unit> {
        return folderRepository.setSortPreference(folderId, sort)
    }
}
