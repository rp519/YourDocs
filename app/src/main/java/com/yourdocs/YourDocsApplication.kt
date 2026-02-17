package com.yourdocs

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yourdocs.data.local.storage.FileStorageManager
import com.yourdocs.data.worker.ExpiryCheckWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class YourDocsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var fileStorageManager: FileStorageManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            fileStorageManager.initialize()
        }

        scheduleExpiryCheck()
    }

    private fun scheduleExpiryCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
