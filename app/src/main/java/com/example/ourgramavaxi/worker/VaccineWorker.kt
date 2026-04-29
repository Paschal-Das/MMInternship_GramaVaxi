package com.example.ourgramavaxi.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ourgramavaxi.notifications.NotificationHelper

class VaccineWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        
        // In a real app, you'd check a database or API for the exact camp date.
        // For this prototype, we trigger the scheduled alert.
        notificationHelper.showNotification(
            title = "Vaccination Camp Alert! (ಲಸಿಕೆ ಶಿಬಿರ)",
            message = "Doctor arriving at Temple Square in 3 days. Please prepare your livestock."
        )

        return Result.success()
    }
}
