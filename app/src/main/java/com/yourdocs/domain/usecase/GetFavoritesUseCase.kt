package com.yourdocs.domain.usecase

import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    operator fun invoke(): Flow<List<DocumentWithFolderInfo>> {
        return documentRepository.observeFavorites()
    }
}
