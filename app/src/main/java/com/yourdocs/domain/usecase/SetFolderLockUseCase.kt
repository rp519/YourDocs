package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.LockMethod
import com.yourdocs.domain.repository.FolderRepository
import javax.inject.Inject

class SetFolderLockUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String, lock: Boolean, lockMethod: LockMethod? = null): Result<Unit> {
        if (lock && lockMethod == null) {
            return Result.failure(IllegalArgumentException("Lock method required when locking a folder"))
        }
        return folderRepository.setLocked(folderId, lock, lockMethod?.name)
    }
}
