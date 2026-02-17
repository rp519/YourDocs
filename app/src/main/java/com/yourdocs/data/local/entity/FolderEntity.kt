package com.yourdocs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.model.LockMethod
import java.time.Instant

/**
 * Room entity representing a folder in the database.
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean,

    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null,

    @ColumnInfo(name = "emoji")
    val emoji: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "lock_method")
    val lockMethod: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Unix timestamp in milliseconds

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long  // Unix timestamp in milliseconds
)

/**
 * Folder entity with document count from joined query.
 */
data class FolderWithCount(
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean,

    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null,

    @ColumnInfo(name = "emoji")
    val emoji: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "lock_method")
    val lockMethod: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "document_count")
    val documentCount: Int
)

// Extension functions for mapping between domain and data layer
fun FolderEntity.toDomain(documentCount: Int = 0): Folder {
    return Folder(
        id = id,
        name = name,
        isPinned = isPinned,
        isLocked = isLocked,
        documentCount = documentCount,
        colorHex = colorHex,
        emoji = emoji,
        description = description,
        lockMethod = lockMethod?.let { try { LockMethod.valueOf(it) } catch (_: Exception) { null } },
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun FolderWithCount.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        isPinned = isPinned,
        isLocked = isLocked,
        documentCount = documentCount,
        colorHex = colorHex,
        emoji = emoji,
        description = description,
        lockMethod = lockMethod?.let { try { LockMethod.valueOf(it) } catch (_: Exception) { null } },
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        isPinned = isPinned,
        isLocked = isLocked,
        colorHex = colorHex,
        emoji = emoji,
        description = description,
        lockMethod = lockMethod?.name,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}
