package com.yourdocs.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yourdocs.domain.model.Tag
import java.time.Instant

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "document_tags",
    primaryKeys = ["document_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tag_id")]
)
data class DocumentTagEntity(
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "tag_id") val tagId: String
)

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        colorHex = colorHex,
        createdAt = Instant.ofEpochMilli(createdAt)
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        createdAt = createdAt.toEpochMilli()
    )
}
