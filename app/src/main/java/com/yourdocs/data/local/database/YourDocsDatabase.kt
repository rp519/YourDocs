package com.yourdocs.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourdocs.data.local.dao.DocumentDao
import com.yourdocs.data.local.dao.FolderDao
import com.yourdocs.data.local.dao.TagDao
import com.yourdocs.data.local.entity.DocumentEntity
import com.yourdocs.data.local.entity.DocumentTagEntity
import com.yourdocs.data.local.entity.FolderEntity
import com.yourdocs.data.local.entity.TagEntity

@Database(
    entities = [
        FolderEntity::class,
        DocumentEntity::class,
        TagEntity::class,
        DocumentTagEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class YourDocsDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun documentDao(): DocumentDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "yourdocs.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE folders ADD COLUMN color_hex TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE folders ADD COLUMN emoji TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE folders ADD COLUMN description TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE folders ADD COLUMN lock_method TEXT DEFAULT NULL")
                db.execSQL("UPDATE folders SET lock_method = 'BIOMETRIC' WHERE is_locked = 1")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // New columns on documents table
                db.execSQL("ALTER TABLE documents ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE documents ADD COLUMN last_viewed_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE documents ADD COLUMN expiry_date INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE documents ADD COLUMN notes TEXT DEFAULT NULL")

                // New column on folders table
                db.execSQL("ALTER TABLE folders ADD COLUMN sort_preference TEXT DEFAULT NULL")

                // Tags table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tags (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        color_hex TEXT NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name COLLATE NOCASE)")

                // Document-tags junction table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS document_tags (
                        document_id TEXT NOT NULL,
                        tag_id TEXT NOT NULL,
                        PRIMARY KEY(document_id, tag_id),
                        FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE,
                        FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_document_tags_tag_id ON document_tags(tag_id)")
            }
        }
    }
}
