package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Use case to delete a document (physical file + DB record).
 * This is a destructive operation that should be confirmed by the user.
 */
class DeleteDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String): Result<Unit> {
        return documentRepository.deleteDocument(documentId)
    }
}
