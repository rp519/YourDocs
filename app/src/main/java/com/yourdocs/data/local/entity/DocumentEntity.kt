package com.yourdocs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import java.time.Instant

/**
 * Room entity representing a document in the database.
 */
@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folder_id"])]
)
data class DocumentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String,

    @ColumnInfo(name = "original_name")
    val originalName: String,

    @ColumnInfo(name = "stored_file_name")
    val storedFileName: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,

    @ColumnInfo(name = "source")
    val source: String, // Stored as string to avoid enum issues

    @ColumnInfo(name = "page_count")
    val pageCount: Int?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

// Extension functions for mapping
fun DocumentEntity.toDomain(): Document {
    return Document(
        id = id,
        folderId = folderId,
        originalName = originalName,
        storedFileName = storedFileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        source = DocumentSource.valueOf(source),
        pageCount = pageCount,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun Document.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        folderId = folderId,
        originalName = originalName,
        storedFileName = storedFileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        source = source.name,
        pageCount = pageCount,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}
