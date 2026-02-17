package com.yourdocs.data.local.entity

import androidx.room.ColumnInfo
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.model.DocumentWithFolderInfo
import java.time.Instant

data class DocumentWithFolder(
    val id: String,
    @ColumnInfo(name = "folder_id") val folderId: String,
    @ColumnInfo(name = "original_name") val originalName: String,
    @ColumnInfo(name = "stored_file_name") val storedFileName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    val source: String,
    @ColumnInfo(name = "page_count") val pageCount: Int?,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "last_viewed_at") val lastViewedAt: Long?,
    @ColumnInfo(name = "expiry_date") val expiryDate: Long?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "folder_name") val folderName: String
)

fun DocumentWithFolder.toDomain(): DocumentWithFolderInfo {
    return DocumentWithFolderInfo(
        document = Document(
            id = id,
            folderId = folderId,
            originalName = originalName,
            storedFileName = storedFileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            source = DocumentSource.valueOf(source),
            pageCount = pageCount,
            isFavorite = isFavorite,
            lastViewedAt = lastViewedAt?.let { Instant.ofEpochMilli(it) },
            expiryDate = expiryDate?.let { Instant.ofEpochMilli(it) },
            notes = notes,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        ),
        folderName = folderName
    )
}
