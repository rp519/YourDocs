package com.yourdocs.di

import com.yourdocs.data.repository.DocumentRepositoryImpl
import com.yourdocs.data.repository.FolderRepositoryImpl
import com.yourdocs.data.repository.TagRepositoryImpl
import com.yourdocs.domain.repository.DocumentRepository
import com.yourdocs.domain.repository.FolderRepository
import com.yourdocs.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        folderRepositoryImpl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        documentRepositoryImpl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: TagRepositoryImpl
    ): TagRepository
}
