package com.yourdocs.data.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.yourdocs.BuildConfig
import com.yourdocs.data.local.dao.DocumentDao
import com.yourdocs.data.local.dao.FolderDao
import com.yourdocs.data.local.storage.FileStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class RestoreResult(
    val foldersRestored: Int,
    val documentsRestored: Int
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val folderDao: FolderDao,
    private val documentDao: DocumentDao,
    private val fileStorageManager: FileStorageManager,
    private val contentResolver: ContentResolver
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun createBackup(onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        onProgress(0.1f)

        // Query all data
        val folders = folderDao.getAllFoldersWithCount()
        val folderEntities = folders.map { fc ->
            folderDao.getFolderById(fc.id)!!
        }
        val documents = documentDao.getAllDocuments()

        onProgress(0.2f)

        // Build metadata
        val metadata = BackupMetadata(
            version = 1,
            createdAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            folders = folderEntities.map { it.toBackup() },
            documents = documents.map { it.toBackup() }
        )

        val metadataJson = json.encodeToString(BackupMetadata.serializer(), metadata)

        // Create zip in cache dir
        val zipFile = File(context.cacheDir, "yourdocs_backup_${System.currentTimeMillis()}.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // Write metadata
            zos.putNextEntry(ZipEntry("metadata.json"))
            zos.write(metadataJson.toByteArray())
            zos.closeEntry()

            onProgress(0.4f)

            // Write document files
            val totalDocs = documents.size
            documents.forEachIndexed { index, doc ->
                val file = fileStorageManager.getDocumentFile(doc.storedFileName)
                if (file.exists()) {
                    zos.putNextEntry(ZipEntry("documents/${doc.storedFileName}"))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
                onProgress(0.4f + 0.5f * (index + 1) / totalDocs.coerceAtLeast(1))
            }
        }

        onProgress(1f)
        zipFile
    }

    suspend fun restoreFromBackup(zipUri: Uri, onProgress: (Float) -> Unit): RestoreResult = withContext(Dispatchers.IO) {
        onProgress(0.1f)

        val inputStream = contentResolver.openInputStream(zipUri)
            ?: throw IllegalStateException("Cannot open backup file")

        var metadata: BackupMetadata? = null
        val docFiles = mutableMapOf<String, ByteArray>()

        // Read zip contents
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "metadata.json" -> {
                        val bytes = zis.readBytes()
                        metadata = json.decodeFromString(BackupMetadata.serializer(), String(bytes))
                    }
                    entry.name.startsWith("documents/") -> {
                        val fileName = entry.name.removePrefix("documents/")
                        if (fileName.isNotEmpty()) {
                            docFiles[fileName] = zis.readBytes()
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        onProgress(0.3f)

        val meta = metadata ?: throw IllegalStateException("Invalid backup: missing metadata")

        // Clear existing data — delete documents first (foreign key constraint)
        val existingDocs = documentDao.getAllDocuments()
        for (doc in existingDocs) {
            fileStorageManager.deleteDocument(doc.storedFileName)
            documentDao.deleteDocumentById(doc.id)
        }
        val existingFolders = folderDao.getAllFoldersWithCount()
        for (folder in existingFolders) {
            folderDao.deleteFolderById(folder.id)
        }

        onProgress(0.5f)

        // Insert folders
        for (backupFolder in meta.folders) {
            folderDao.insertFolder(backupFolder.toEntity())
        }

        onProgress(0.6f)

        // Write document files and insert records
        val totalDocs = meta.documents.size
        meta.documents.forEachIndexed { index, backupDoc ->
            val fileBytes = docFiles[backupDoc.storedFileName]
            if (fileBytes != null) {
                val docFile = fileStorageManager.getDocumentFile(backupDoc.storedFileName)
                docFile.writeBytes(fileBytes)
            }

            documentDao.insertDocument(backupDoc.toEntity())

            onProgress(0.6f + 0.4f * (index + 1) / totalDocs.coerceAtLeast(1))
        }

        RestoreResult(
            foldersRestored = meta.folders.size,
            documentsRestored = meta.documents.size
        )
    }
}
