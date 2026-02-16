package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all folders with document counts.
 * Folders are automatically sorted: pinned first, then alphabetically.
 */
class GetAllFoldersUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return folderRepository.observeAllFolders()
    }
}
