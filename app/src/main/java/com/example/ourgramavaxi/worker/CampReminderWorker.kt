package com.example.ourgramavaxi.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ourgramavaxi.data.CampAlertDao
import com.example.ourgramavaxi.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class CampReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val campAlertDao: CampAlertDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val notificationHelper = NotificationHelper(applicationContext)

            val upcomingCamps = campAlertDao.getUpcomingCamps(System.currentTimeMillis()).first()
            val now = System.currentTimeMillis()
            val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

            val campsInThreeDays = upcomingCamps.filter { camp ->
                val timeToCamp = camp.date - now
                timeToCamp in 0..threeDaysMs
            }

            campsInThreeDays.forEach { camp ->
                notificationHelper.showNotification(
                    title = "Vaccination Camp Alert! (ಲಸಿಕಾ ಶಿದಿರ)",
                    message = "Camp at ${camp.location} on ${camp.date}",
                    notificationId = camp.id
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}