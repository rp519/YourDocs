package com.yourdocs.domain.repository

import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {

    fun observeAllTags(): Flow<List<Tag>>

    suspend fun createTag(name: String, colorHex: String): Result<Tag>

    suspend fun deleteTag(tagId: String): Result<Unit>

    fun observeTagsForDocument(documentId: String): Flow<List<Tag>>

    suspend fun addTagToDocument(documentId: String, tagId: String): Result<Unit>

    suspend fun removeTagFromDocument(documentId: String, tagId: String): Result<Unit>

    fun observeDocumentsByTag(tagId: String): Flow<List<DocumentWithFolderInfo>>
}
