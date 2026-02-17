package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.Document
import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

class FindDuplicateDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(folderId: String, name: String, sizeBytes: Long): Document? {
        return documentRepository.findDuplicate(folderId, name, sizeBytes)
    }
}
