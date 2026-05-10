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

                // ✅ BUG D FIX: Removed "now < dueDate" condition.
                //
                // OLD: !vacc.isCompleted && dueDate > 0 && now >= notifyStartTime && now < dueDate
                // Problem: Once the due date passes, "now < dueDate" is permanently false.
                // If Doze mode or a dead battery delayed the worker past the 3-day window,
                // the farmer gets ZERO notification — even for a very overdue animal.
                //
                // NEW: Notify for anything within the window OR already overdue.
                // The "isCompleted = 0" in the DAO query already ensures we stop notifying
                // once the farmer updates the animal's vaccination record as given.
                !vacc.isCompleted && dueDate > 0 && now >= notifyStartTime
            }

            dueForNotification.forEach { vacc ->
                val animal = animalDao.getAnimalById(vacc.animalId)
                val notificationId = (vacc.animalId * 10000 + vacc.id).toInt()

                // ✅ BUG D FIX: Calculate actual days remaining for an accurate message.
                // Old message always said "in 3 days" regardless of real due date.
                val dueDate = vacc.nextDueDate ?: 0L
                val daysRemaining = (dueDate - now) / (24 * 60 * 60 * 1000L)

                val urgencyText = when {
                    daysRemaining < 0  -> "OVERDUE by ${-daysRemaining} day(s)! (ತಡವಾಗಿದೆ)"
                    daysRemaining == 0L -> "DUE TODAY! (ಇಂದು)"
                    daysRemaining == 1L -> "due TOMORROW (ನಾಳೆ)"
                    else               -> "due in $daysRemaining days"
                }

                notificationHelper.showNotification(
                    title = "ಲಸಿಕೆ ಸಮಯ! — ${animal?.name ?: "Animal"}",
                    message = "${vacc.vaccineName} is $urgencyText.",
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