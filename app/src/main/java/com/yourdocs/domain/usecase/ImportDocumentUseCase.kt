package com.yourdocs.domain.usecase

import android.net.Uri
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.model.FreeLimitReachedException
import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Use case to import a document from a content URI into a folder.
 */
class ImportDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val checkFreeLimitsUseCase: CheckFreeLimitsUseCase
) {
    suspend operator fun invoke(
        folderId: String,
        uri: Uri,
        source: DocumentSource,
        overrideName: String? = null,
        pageCount: Int? = null
    ): Result<Document> {
        val limits = checkFreeLimitsUseCase()
        if (!limits.canCreateDocument) {
            return Result.failure(FreeLimitReachedException("document"))
        }

        return documentRepository.importDocument(folderId, uri, source, overrideName, pageCount)
    }
}
