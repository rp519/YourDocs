package com.yourdocs.domain.usecase

import com.yourdocs.domain.repository.DocumentRepository
import java.time.Instant
import javax.inject.Inject

class SetExpiryDateUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String, expiryDate: Instant?): Result<Unit> {
        if (expiryDate != null && expiryDate.isBefore(Instant.now())) {
            return Result.failure(IllegalArgumentException("Expiry date must be in the future"))
        }
        return documentRepository.setExpiryDate(documentId, expiryDate)
    }
}
