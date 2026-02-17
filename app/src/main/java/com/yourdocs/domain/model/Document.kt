package com.yourdocs.domain.model

import java.time.Instant

/**
 * Domain model representing a document stored in a folder.
 */
data class Document(
    val id: String,
    val folderId: String,
    val originalName: String,
    val storedFileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val source: DocumentSource,
    val pageCount: Int?,
    val isFavorite: Boolean = false,
    val lastViewedAt: Instant? = null,
    val expiryDate: Instant? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }

    val extension: String
        get() = originalName.substringAfterLast('.', "")
}

/**
 * Represents the source from which a document was added.
 */
enum class DocumentSource {
    IMPORT,    // Imported from internal storage
    GALLERY,   // Imported from photo gallery
    CAMERA     // Captured with camera
}
