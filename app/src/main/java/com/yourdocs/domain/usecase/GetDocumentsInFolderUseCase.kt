package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.Document
import com.yourdocs.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe documents in a specific folder.
 * Documents are automatically sorted by most recent first.
 */
class GetDocumentsInFolderUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    operator fun invoke(folderId: String): Flow<List<Document>> {
        return documentRepository.observeDocumentsInFolder(folderId)
    }
}
