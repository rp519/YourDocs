package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    operator fun invoke(query: String): Flow<List<DocumentWithFolderInfo>> {
        return documentRepository.searchDocuments(query)
    }
}
