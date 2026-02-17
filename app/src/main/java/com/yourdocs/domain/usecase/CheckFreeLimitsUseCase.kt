package com.yourdocs.domain.usecase

import com.yourdocs.data.billing.PremiumRepository
import com.yourdocs.data.local.dao.DocumentDao
import com.yourdocs.data.local.dao.FolderDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class FreeLimitStatus(
    val isPremium: Boolean,
    val folderCount: Int,
    val documentCount: Int,
    val canCreateFolder: Boolean,
    val canCreateDocument: Boolean,
    val maxFolders: Int = MAX_FREE_FOLDERS,
    val maxDocuments: Int = MAX_FREE_DOCUMENTS
) {
    companion object {
        const val MAX_FREE_FOLDERS = 5
        const val MAX_FREE_DOCUMENTS = 25
    }
}

private const val MAX_FREE_FOLDERS = 5
private const val MAX_FREE_DOCUMENTS = 25

class CheckFreeLimitsUseCase @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val folderDao: FolderDao,
    private val documentDao: DocumentDao
) {
    suspend operator fun invoke(): FreeLimitStatus {
        val isPremium = premiumRepository.isPremium.first()
        val folderCount = folderDao.getTotalFolderCount()
        val documentCount = documentDao.getTotalDocumentCount()

        return FreeLimitStatus(
            isPremium = isPremium,
            folderCount = folderCount,
            documentCount = documentCount,
            canCreateFolder = isPremium || folderCount < MAX_FREE_FOLDERS,
            canCreateDocument = isPremium || documentCount < MAX_FREE_DOCUMENTS
        )
    }
}
