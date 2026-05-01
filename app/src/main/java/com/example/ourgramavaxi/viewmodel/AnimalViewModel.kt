package com.example.ourgramavaxi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.*
import com.example.ourgramavaxi.worker.VaccineWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AnimalViewModel(application: Application, private val animalDao: AnimalDao) : AndroidViewModel(application) {

    val allAnimals: Flow<List<Animal>> = animalDao.getAllAnimals()
    val allUpcomingVaccinations: Flow<List<Vaccination>> = animalDao.getAllUpcomingVaccinations()
    val allCampAlerts: Flow<List<CampAlert>> = animalDao.getAllCampAlerts()

    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>> {
        return animalDao.getVaccinationsForAnimal(animalId)
    }

    private val _currentLanguage = MutableStateFlow("en") // "en" or "kn"
    val currentLanguage = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == "en") "kn" else "en"
    }

    fun addAnimal(
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageInYears: Int,
        district: String = "Mandya",
        notes: String = "",
        photoUri: String? = null,
        lastVaccineDates: Map<String, Long?> = emptyMap(),
        nextVaccineDates: Map<String, Long?> = emptyMap()
    ) {
        viewModelScope.launch {
            val newAnimal = Animal(
                name = name,
                species = species,
                breed = breed,
                gender = gender,
                ageInYears = ageInYears,
                district = district,
                notes = notes,
                photoUri = photoUri
            )
            val id = animalDao.insertAnimal(newAnimal).toInt()

            // Process provided vaccination history
            lastVaccineDates.forEach { (vaccineName, lastDate) ->
                if (lastDate != null) {
                    // Use user-provided next date if available, otherwise calculate
                    val nextDate = nextVaccineDates[vaccineName] ?: (lastDate + (365L * 24 * 60 * 60 * 1000L))
                    
                    // Record the past dose
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id,
                            vaccineName = vaccineName,
                            dateAdministered = lastDate,
                            nextDueDate = nextDate,
                            isCompleted = true
                        )
                    )
                }
            }

            // Handle vaccines where only next date is provided (pending dose)
            nextVaccineDates.forEach { (vaccineName, nextDate) ->
                if (nextDate != null && !lastVaccineDates.containsKey(vaccineName)) {
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id,
                            vaccineName = vaccineName,
                            dateAdministered = 0,
                            nextDueDate = nextDate,
                            isCompleted = false
                        )
                    )
                }
            }

            // Check for Zone-Specific Requirements (e.g. Anthrax)
            VaccineConstants.HOTSPOT_ZONES.forEach { (vaccine, districts) ->
                if (districts.contains(district) && !lastVaccineDates.containsKey(vaccine)) {
                    // Schedule a pending vaccination if in hotspot and not already vaccinated
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id,
                            vaccineName = vaccine,
                            dateAdministered = 0, // Never administered
                            nextDueDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L), // Suggest within 2 days
                            isCompleted = false
                        )
                    )
                }
            }

            // Always schedule a general health checkup if no history is provided
            if (lastVaccineDates.isEmpty()) {
                animalDao.insertVaccination(
                    Vaccination(
                        animalId = id,
                        vaccineName = "Initial Health Checkup",
                        dateAdministered = System.currentTimeMillis(),
                        nextDueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                        isCompleted = false
                    )
                )
            }

            scheduleVaccineReminder()
        }
    }

    private fun scheduleVaccineReminder() {
        // Use a PeriodicWorkRequest to check daily
        val workRequest = PeriodicWorkRequestBuilder<VaccineWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .addTag("vaccine_periodic_check")
            .build()
        
        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "vaccine_reminder_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch {
            animalDao.deleteAnimal(animal)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            // Check if data already exists to avoid duplicates
            val count = animalDao.getAnimalCount()
            if (count > 0) return@launch

            // Seed Sheep
            val sheepId = animalDao.insertAnimal(
                Animal(name = "Muttu", species = "Sheep", breed = "Yelaga", gender = "Male", ageInYears = 1, notes = "Healthy ram")
            ).toInt()
            
            // Seed Goat
            val goatId = animalDao.insertAnimal(
                Animal(name = "Gauri", species = "Goat", breed = "Sirohi", gender = "Female", ageInYears = 2, notes = "Due for PPR")
            ).toInt()

            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L

            // Muttu's FMD (Recent)
            animalDao.insertVaccination(Vaccination(
                animalId = sheepId,
                vaccineName = VaccineConstants.FMD,
                dateAdministered = now - (30 * day),
                nextDueDate = now + (150 * day),
                isCompleted = true
            ))

            // Gauri's PPR (Overdue)
            animalDao.insertVaccination(Vaccination(
                animalId = goatId,
                vaccineName = VaccineConstants.PPR,
                dateAdministered = now - (400 * day),
                nextDueDate = now - (35 * day), // Overdue by a month
                isCompleted = false
            ))

            // Seed Camp Alerts
            val calendar = Calendar.getInstance()
            
            // FMD Camp (April/Oct)
            calendar.set(Calendar.MONTH, Calendar.APRIL)
            calendar.set(Calendar.DAY_OF_MONTH, 15)
            animalDao.insertCampAlert(CampAlert(
                titleResId = R.string.fmd_camp_title,
                descResId = R.string.fmd_camp_desc,
                locationResId = R.string.panchayat_office,
                date = calendar.timeInMillis,
                typeResId = R.string.vaccination_camp_type
            ))

            // HS Camp (Pre-monsoon May)
            calendar.set(Calendar.MONTH, Calendar.MAY)
            calendar.set(Calendar.DAY_OF_MONTH, 10)
            animalDao.insertCampAlert(CampAlert(
                titleResId = R.string.hs_camp_title,
                descResId = R.string.hs_camp_desc,
                locationResId = R.string.community_hall,
                date = calendar.timeInMillis,
                typeResId = R.string.health_drive_type
            ))
        }
    }
}

class AnimalViewModelFactory(private val application: Application, private val animalDao: AnimalDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnimalViewModel(application, animalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
