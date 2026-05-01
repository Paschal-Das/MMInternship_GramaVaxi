package com.example.ourgramavaxi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
        lastVaccineDates: Map<String, Long?> = emptyMap()
    ) {
        viewModelScope.launch {
            val newAnimal = Animal(
                name = name,
                species = species,
                breed = breed,
                gender = gender,
                ageInYears = ageInYears,
                district = district
            )
            val id = animalDao.insertAnimal(newAnimal).toInt()

            // Process provided vaccination history and schedule next doses
            lastVaccineDates.forEach { (vaccineName, lastDate) ->
                if (lastDate != null) {
                    val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365
                    val nextDate = lastDate + (intervalDays.toLong() * 24 * 60 * 60 * 1000L)
                    
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
        val workRequest = OneTimeWorkRequestBuilder<VaccineWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag("vaccine_reminder")
            .build()
        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch {
            animalDao.deleteAnimal(animal)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            // Seed Animal
            val id = animalDao.insertAnimal(
                Animal(name = "Gauri", species = "Goat", breed = "Bidri", gender = "Female", ageInYears = 2)
            ).toInt()
            
            val lastFmd = System.currentTimeMillis() - (100L * 24 * 60 * 60 * 1000L) // 100 days ago
            animalDao.insertVaccination(Vaccination(
                animalId = id,
                vaccineName = VaccineConstants.FMD,
                dateAdministered = lastFmd,
                nextDueDate = lastFmd + (180L * 24 * 60 * 60 * 1000L),
                isCompleted = true
            ))

            // Seed Camp Alerts
            val calendar = Calendar.getInstance()
            
            // FMD Camp (April/Oct)
            calendar.set(Calendar.MONTH, Calendar.APRIL)
            calendar.set(Calendar.DAY_OF_MONTH, 15)
            animalDao.insertCampAlert(CampAlert(
                title = "FMD Vaccination Camp",
                description = "Government organized FMD vaccination drive for all livestock in the village.",
                location = "Grama Panchayat Office",
                date = calendar.timeInMillis,
                type = "Vaccination Camp"
            ))

            // HS Camp (Pre-monsoon May)
            calendar.set(Calendar.MONTH, Calendar.MAY)
            calendar.set(Calendar.DAY_OF_MONTH, 10)
            animalDao.insertCampAlert(CampAlert(
                title = "HS & BQ Prevention Drive",
                description = "Protect your sheep and goats from Haemorrhagic Septicaemia before monsoon starts.",
                location = "Community Hall",
                date = calendar.timeInMillis,
                type = "Health Drive"
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
