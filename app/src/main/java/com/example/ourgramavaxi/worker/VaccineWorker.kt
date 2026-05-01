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
        
        // Check for upcoming vaccinations in the next 7 days
        val upcoming = animalDao.getAllUpcomingVaccinations().first()
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7L * 24 * 60 * 60 * 1000L)
        
        val overdueOrNear = upcoming.filter { 
            it.nextDueDate != null && it.nextDueDate <= sevenDaysFromNow && !it.isCompleted
        }

        if (overdueOrNear.isNotEmpty()) {
            val animal = animalDao.getAnimalById(overdueOrNear.first().animalId)
            notificationHelper.showNotification(
                title = "Vaccination Due! (ಲಸಿಕೆ ಬಾಕಿ ಇದೆ)",
                message = "${animal?.name ?: "Your animal"} is due for ${overdueOrNear.first().vaccineName} soon."
            )
        } else {
            // Fallback for prototype visibility
            notificationHelper.showNotification(
                title = "Grama-Vaxi Update",
                message = "Your livestock records are being monitored for health alerts."
            )
        }

        return Result.success()
    }
}
