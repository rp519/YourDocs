package com.yourdocs.domain.usecase

import android.net.Uri
import com.yourdocs.data.backup.BackupManager
import com.yourdocs.data.backup.RestoreResult
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(zipUri: Uri, onProgress: (Float) -> Unit): RestoreResult {
        return backupManager.restoreFromBackup(zipUri, onProgress)
    }
}
