package com.yourdocs.domain.usecase

import android.net.Uri
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Use case to import a document from a content URI into a folder.
 */
class ImportDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(
        folderId: String,
        uri: Uri,
        source: DocumentSource,
        overrideName: String? = null,
        pageCount: Int? = null
    ): Result<Document> {
        return documentRepository.importDocument(folderId, uri, source, overrideName, pageCount)
    }
}
