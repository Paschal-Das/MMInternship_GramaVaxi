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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class CampReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val campAlertDao: CampAlertDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val notificationHelper = NotificationHelper(applicationContext)

            // ✅ BUG 3 FIX: Format the date into a readable string instead of showing raw millis
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val upcomingCamps = campAlertDao.getUpcomingCamps(System.currentTimeMillis()).first()
            val now = System.currentTimeMillis()
            val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

            val campsInThreeDays = upcomingCamps.filter { camp ->
                val timeToCamp = camp.date - now
                timeToCamp in 0..threeDaysMs
            }

            campsInThreeDays.forEach { camp ->
                // ✅ BUG 3 FIX: Use formatted date string in notification message
                val formattedDate = dateFormat.format(Date(camp.date))
                notificationHelper.showNotification(
                    title = applicationContext.getString(
                        android.R.string.ok // placeholder — see note below
                    ).let { "ಲಸಿಕಾ ಶಿದಿರ ಎಚ್ಚರಿಕೆ! (Vaccination Camp Alert)"},
                    message = "${camp.title} - ${camp.location} - $formattedDate",
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