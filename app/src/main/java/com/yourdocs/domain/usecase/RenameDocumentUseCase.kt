package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Use case to rename a document.
 * Validates that new name is not empty.
 */
class RenameDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String, newName: String): Result<Unit> {
        val trimmedName = newName.trim()

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Document name cannot be empty"))
        }

        if (trimmedName.length > MAX_DOCUMENT_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Document name too long (max $MAX_DOCUMENT_NAME_LENGTH characters)")
            )
        }

        return documentRepository.renameDocument(documentId, trimmedName)
    }

    companion object {
        private const val MAX_DOCUMENT_NAME_LENGTH = 200
    }
}
