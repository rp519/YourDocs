package com.yourdocs.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourdocs.data.notification.ExpiryNotificationManager
import com.yourdocs.domain.repository.DocumentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class ExpiryCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val documentRepository: DocumentRepository,
    private val notificationManager: ExpiryNotificationManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val documents = documentRepository.getDocumentsWithExpiry()
            val now = Instant.now()

            documents.forEachIndexed { index, document ->
                val expiryDate = document.expiryDate ?: return@forEachIndexed
                val daysUntil = ChronoUnit.DAYS.between(now, expiryDate)

                if (daysUntil <= 30) {
                    notificationManager.showExpiryNotification(
                        documentName = document.originalName,
                        daysUntilExpiry = daysUntil,
                        notificationId = 10000 + index
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
