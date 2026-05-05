package com.example.ourgramavaxi.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ourgramavaxi.data.AppDatabase
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.notifications.NotificationHelper
import kotlinx.coroutines.flow.first

class VaccineWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val animalDao = database.animalDao()

        val upcoming = animalDao.getAllUpcomingVaccinations().first()
        val now = System.currentTimeMillis()

        // ✅ Bug 13: Use named constant instead of magic number
        val notifyWindowMs = VaccineConstants.NOTIFICATION_DAYS * 24 * 60 * 60 * 1000L

        val dueForNotification = upcoming.filter { vacc ->
            val dueDate = vacc.nextDueDate ?: 0L
            val notifyStartTime = dueDate - notifyWindowMs
            !vacc.isCompleted && dueDate > 0 && now >= notifyStartTime && now < dueDate
        }

        dueForNotification.forEach { vacc ->
            val animal = animalDao.getAnimalById(vacc.animalId)
            notificationHelper.showNotification(
                title = "Upcoming Vaccine! (ಲಸಿಕೆ ಸಮಯ)",
                message = "${animal?.name ?: "Animal"} is due for ${vacc.vaccineName} in ${VaccineConstants.NOTIFICATION_DAYS} days.",
                // ✅ Bug 7 fix: Use vaccination record ID as stable unique notification ID
                notificationId = vacc.id
            )
        }

        return Result.success()
    }
}