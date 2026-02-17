package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

class UpdateDocumentNotesUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String, notes: String?): Result<Unit> {
        val cleanedNotes = notes?.trim()?.ifEmpty { null }
        return documentRepository.updateNotes(documentId, cleanedNotes)
    }
}
