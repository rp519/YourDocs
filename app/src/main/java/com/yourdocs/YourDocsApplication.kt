package com.yourdocs

import android.app.Application
import com.yourdocs.data.local.storage.FileStorageManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for YourDocs.
 * Initializes Hilt and storage on startup.
 */
@HiltAndroidApp
class YourDocsApplication : Application() {

    @Inject
    lateinit var fileStorageManager: FileStorageManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize storage directories
        applicationScope.launch {
            fileStorageManager.initialize()
        }
    }
}
