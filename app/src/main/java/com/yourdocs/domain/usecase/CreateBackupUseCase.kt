package com.yourdocs.domain.usecase

import com.yourdocs.data.backup.BackupManager
import java.io.File
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(onProgress: (Float) -> Unit): File {
        return backupManager.createBackup(onProgress)
    }
}
