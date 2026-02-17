package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

class DeleteMultipleDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(ids: List<String>): Result<Unit> {
        if (ids.isEmpty()) return Result.success(Unit)
        return documentRepository.deleteDocuments(ids)
    }
}
