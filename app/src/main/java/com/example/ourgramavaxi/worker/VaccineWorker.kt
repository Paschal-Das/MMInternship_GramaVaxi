package com.example.ourgramavaxi.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ourgramavaxi.data.AnimalDao
import com.example.ourgramavaxi.data.VaccinationDao
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class VaccineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val vaccinationDao: VaccinationDao,
    private val animalDao: AnimalDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val notificationHelper = NotificationHelper(applicationContext)

            val upcoming = vaccinationDao.getAllUpcomingVaccinations().first()
            val now = System.currentTimeMillis()
            val notifyWindowMs = VaccineConstants.NOTIFICATION_DAYS * 24 * 60 * 60 * 1000L

            val dueForNotification = upcoming.filter { vacc ->
                val dueDate = vacc.nextDueDate ?: 0L
                val notifyStartTime = dueDate - notifyWindowMs
                !vacc.isCompleted && dueDate > 0 && now >= notifyStartTime && now < dueDate
            }

            dueForNotification.forEach { vacc ->
                val animal = animalDao.getAnimalById(vacc.animalId)
                val notificationId = (vacc.animalId * 10000 + vacc.id).toInt()  // FIX BUG #4

                notificationHelper.showNotification(
                    title = "Upcoming Vaccine! (ಲಸಿಕೆ ಸಮಯ)",
                    message = "${animal?.name ?: "Animal"} is due for ${vacc.vaccineName} in ${VaccineConstants.NOTIFICATION_DAYS} days.",
                    notificationId = notificationId
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}