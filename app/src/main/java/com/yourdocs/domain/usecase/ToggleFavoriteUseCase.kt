package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String): Result<Unit> {
        return documentRepository.toggleFavorite(documentId)
    }
}
