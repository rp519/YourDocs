package com.yourdocs.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourdocs.data.local.dao.DocumentDao
import com.yourdocs.data.local.dao.FolderDao
import com.yourdocs.data.local.entity.DocumentEntity
import com.yourdocs.data.local.entity.FolderEntity

/**
 * Room database for YourDocs app.
 */
@Database(
    entities = [
        FolderEntity::class,
        DocumentEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class YourDocsDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun documentDao(): DocumentDao

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
    }
}
