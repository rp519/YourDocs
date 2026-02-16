package com.yourdocs.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.yourdocs.data.local.dao.DocumentDao
import com.yourdocs.data.local.dao.FolderDao
import com.yourdocs.data.local.database.YourDocsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideYourDocsDatabase(
        @ApplicationContext context: Context
    ): YourDocsDatabase {
        return Room.databaseBuilder(
            context,
            YourDocsDatabase::class.java,
            YourDocsDatabase.DATABASE_NAME
        )
            .addMigrations(YourDocsDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideFolderDao(database: YourDocsDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideDocumentDao(database: YourDocsDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }
}
