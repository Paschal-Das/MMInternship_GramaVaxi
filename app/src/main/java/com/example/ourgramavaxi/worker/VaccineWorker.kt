package com.example.ourgramavaxi.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ourgramavaxi.data.AppDatabase
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
        
        // Check for upcoming vaccinations
        val upcoming = animalDao.getAllUpcomingVaccinations().first()
        val now = System.currentTimeMillis()
        
        // Target: Notify 3 days before nextDueDate
        // Notification Window: Between (nextDueDate - 3 days) and nextDueDate
        val threeDaysInMs = 3L * 24 * 60 * 60 * 1000L
        
        val dueForNotification = upcoming.filter { vacc ->
            val dueDate = vacc.nextDueDate ?: 0L
            val notifyStartTime = dueDate - threeDaysInMs
            
            !vacc.isCompleted && dueDate > 0 && now >= notifyStartTime && now < dueDate
        }

        dueForNotification.forEach { vacc ->
            val animal = animalDao.getAnimalById(vacc.animalId)
            notificationHelper.showNotification(
                title = "Upcoming Vaccine! (ಲಸಿಕೆ ಸಮಯ)",
                message = "${animal?.name ?: "Animal"} is due for ${vacc.vaccineName} in 3 days."
            )
        }

        return Result.success()
    }
}
