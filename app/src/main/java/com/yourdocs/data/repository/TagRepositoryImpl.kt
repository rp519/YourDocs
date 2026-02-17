package com.yourdocs.data.repository

import com.yourdocs.data.local.dao.TagDao
import com.yourdocs.data.local.entity.DocumentTagEntity
import com.yourdocs.data.local.entity.toDomain
import com.yourdocs.data.local.entity.toEntity
import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.model.Tag
import com.yourdocs.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun observeAllTags(): Flow<List<Tag>> {
        return tagDao.observeAllTags().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createTag(name: String, colorHex: String): Result<Tag> {
        return try {
            val existing = tagDao.getTagByName(name)
            if (existing != null) {
                return Result.failure(IllegalArgumentException("Tag name already exists"))
            }
            val tag = Tag(
                id = Tag.generateId(),
                name = name.trim(),
                colorHex = colorHex,
                createdAt = Instant.now()
            )
            tagDao.insertTag(tag.toEntity())
            Result.success(tag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(tagId: String): Result<Unit> {
        return try {
            tagDao.deleteTag(tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeTagsForDocument(documentId: String): Flow<List<Tag>> {
        return tagDao.observeTagsForDocument(documentId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addTagToDocument(documentId: String, tagId: String): Result<Unit> {
        return try {
            tagDao.addTagToDocument(DocumentTagEntity(documentId, tagId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeTagFromDocument(documentId: String, tagId: String): Result<Unit> {
        return try {
            tagDao.removeTagFromDocument(documentId, tagId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeDocumentsByTag(tagId: String): Flow<List<DocumentWithFolderInfo>> {
        return tagDao.observeDocumentsByTag(tagId)
            .map { list -> list.map { it.toDomain() } }
    }
}
