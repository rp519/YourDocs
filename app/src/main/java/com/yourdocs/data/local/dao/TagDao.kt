package com.yourdocs.data.local.dao

import androidx.room.*
import com.yourdocs.data.local.entity.DocumentTagEntity
import com.yourdocs.data.local.entity.DocumentWithFolder
import com.yourdocs.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): TagEntity?

    @Query("SELECT * FROM tags WHERE LOWER(name) = LOWER(:name)")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTagToDocument(documentTag: DocumentTagEntity)

    @Query("DELETE FROM document_tags WHERE document_id = :documentId AND tag_id = :tagId")
    suspend fun removeTagFromDocument(documentId: String, tagId: String)

    @Query("SELECT t.* FROM tags t JOIN document_tags dt ON t.id = dt.tag_id WHERE dt.document_id = :documentId ORDER BY t.name ASC")
    fun observeTagsForDocument(documentId: String): Flow<List<TagEntity>>

    @Query("SELECT t.* FROM tags t JOIN document_tags dt ON t.id = dt.tag_id WHERE dt.document_id = :documentId ORDER BY t.name ASC")
    suspend fun getTagsForDocument(documentId: String): List<TagEntity>

    @Query("SELECT d.*, f.name as folder_name FROM documents d JOIN document_tags dt ON d.id = dt.document_id JOIN folders f ON d.folder_id = f.id WHERE dt.tag_id = :tagId ORDER BY d.updated_at DESC")
    fun observeDocumentsByTag(tagId: String): Flow<List<DocumentWithFolder>>
}
