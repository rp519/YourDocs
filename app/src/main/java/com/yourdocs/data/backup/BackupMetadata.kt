package com.yourdocs.data.backup

import com.yourdocs.data.local.entity.DocumentEntity
import com.yourdocs.data.local.entity.FolderEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val version: Int = 1,
    val createdAt: Long,
    val appVersion: String,
    val folders: List<BackupFolder>,
    val documents: List<BackupDocument>
)

@Serializable
data class BackupFolder(
    val id: String,
    val name: String,
    val isPinned: Boolean,
    val isLocked: Boolean,
    val colorHex: String? = null,
    val emoji: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupDocument(
    val id: String,
    val folderId: String,
    val originalName: String,
    val storedFileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val source: String,
    val pageCount: Int?,
    val createdAt: Long,
    val updatedAt: Long
)

fun FolderEntity.toBackup(): BackupFolder {
    return BackupFolder(
        id = id,
        name = name,
        isPinned = isPinned,
        isLocked = isLocked,
        colorHex = colorHex,
        emoji = emoji,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun BackupFolder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        isPinned = isPinned,
        isLocked = isLocked,
        colorHex = colorHex,
        emoji = emoji,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun DocumentEntity.toBackup(): BackupDocument {
    return BackupDocument(
        id = id,
        folderId = folderId,
        originalName = originalName,
        storedFileName = storedFileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        source = source,
        pageCount = pageCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun BackupDocument.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        folderId = folderId,
        originalName = originalName,
        storedFileName = storedFileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        source = source,
        pageCount = pageCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
