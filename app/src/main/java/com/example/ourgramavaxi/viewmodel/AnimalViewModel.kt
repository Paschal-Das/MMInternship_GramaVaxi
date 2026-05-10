package com.example.ourgramavaxi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import android.content.Context
import com.example.ourgramavaxi.data.*
import com.example.ourgramavaxi.domain.usecase.RegisterAnimalUseCase
import com.example.ourgramavaxi.domain.usecase.UpdateAnimalUseCase
import com.example.ourgramavaxi.repository.AnimalRepository
import com.example.ourgramavaxi.repository.PreferenceRepository
import com.example.ourgramavaxi.worker.VaccineWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AnimalViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AnimalRepository,
    private val preferenceRepository: PreferenceRepository,
    private val registerAnimalUseCase: RegisterAnimalUseCase,
    private val updateAnimalUseCase: UpdateAnimalUseCase
) : ViewModel() {

    val allAnimals: Flow<List<Animal>> = repository.getAllAnimals()
    val allUpcomingVaccinations: Flow<List<Vaccination>> = repository.getAllUpcomingVaccinations()
    val allCampAlerts: Flow<List<CampAlert>> = repository.getAllCampAlerts()

    val currentLanguage: StateFlow<String> = preferenceRepository.currentLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val registeredAlertIds: StateFlow<Set<Int>> = preferenceRepository.registeredAlertIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>> {
        return repository.getVaccinationsForAnimal(animalId)
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = currentLanguage.value
            val next = if (current == "en") "kn" else "en"
            preferenceRepository.setLanguage(next)
        }
    }

    fun toggleAlertRegistration(alertId: Int) {
        viewModelScope.launch {
            preferenceRepository.toggleAlertRegistration(alertId)
        }
    }

    fun addCampAlert(title: String, description: String, location: String, date: Long, type: String) {
        viewModelScope.launch {
            repository.insertCampAlert(
                CampAlert(
                    title = title,
                    description = description,
                    location = location,
                    date = date,
                    type = type
                )
            )
        }
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
            registerAnimalUseCase(
                name, species, breed, gender, ageInYears, district, notes, photoUri,
                lastVaccineDates, nextVaccineDates
            )
            scheduleVaccineReminder()
        }
    }

    fun updateAnimal(
        id: Int, name: String, species: String, breed: String,
        gender: String, ageInYears: Int, district: String, notes: String,
        photoUri: String?, lastVaccineDates: Map<String, Long?>, nextVaccineDates: Map<String, Long?>
    ) {
        viewModelScope.launch {
            updateAnimalUseCase(
                id, name, species, breed, gender, ageInYears, district, notes, photoUri,
                lastVaccineDates, nextVaccineDates
            )
            scheduleVaccineReminder()
        }
    }

    private fun scheduleVaccineReminder() {
        val workRequest = PeriodicWorkRequestBuilder<VaccineWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .addTag("vaccine_periodic_check")
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "vaccine_reminder_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch { repository.deleteAnimal(animal) }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val count = repository.getAnimalCount().first()
            if (count > 0) return@launch

            val sheepId = repository.insertAnimal(
                Animal(name = "Muttu", species = "Sheep", breed = "Deccani", gender = "Male", ageInYears = 1, notes = "Healthy ram")
            ).toInt()
            val goatId = repository.insertAnimal(
                Animal(name = "Gauri", species = "Goat", breed = "Osmanabadi", gender = "Female", ageInYears = 2, notes = "Due for PPR")
            ).toInt()

            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L

            repository.insertVaccination(Vaccination(
                animalId = sheepId, vaccineName = VaccineConstants.FMD,
                dateAdministered = now - (30 * day), nextDueDate = now + (150 * day), isCompleted = true
            ))
            repository.insertVaccination(Vaccination(
                animalId = goatId, vaccineName = VaccineConstants.PPR,
                dateAdministered = now - (400 * day), nextDueDate = now - (35 * day), isCompleted = false
            ))

            val calendar = Calendar.getInstance()

            calendar.set(Calendar.MONTH, Calendar.APRIL)
            calendar.set(Calendar.DAY_OF_MONTH, 15)
            repository.insertCampAlert(CampAlert(
                title = "FMD Vaccination Camp",
                description = "Government organized FMD vaccination drive for all livestock in the village.",
                location = "Grama Panchayat Office",
                date = calendar.timeInMillis,
                type = "Vaccination Camp"
            ))

            calendar.set(Calendar.MONTH, Calendar.MAY)
            calendar.set(Calendar.DAY_OF_MONTH, 10)
            repository.insertCampAlert(CampAlert(
                title = "HS & BQ Prevention Drive",
                description = "Protect your sheep and goats from Haemorrhagic Septicaemia before monsoon starts.",
                location = "Community Hall",
                date = calendar.timeInMillis,
                type = "Health Drive"
            ))
        }
    }
}
