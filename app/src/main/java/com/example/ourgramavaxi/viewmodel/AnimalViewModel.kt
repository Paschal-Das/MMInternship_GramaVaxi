package com.example.ourgramavaxi.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
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

    // ─── SharedPreferences — persists language + camp registrations across restarts ───
    // BUG 3 FIX + BUG 4 FIX: Use SharedPreferences so data survives app kill/restart
    private val prefs = application.getSharedPreferences("grama_vaxi_prefs", Context.MODE_PRIVATE)

    val allAnimals: Flow<List<Animal>> = animalDao.getAllAnimals()
    val allUpcomingVaccinations: Flow<List<Vaccination>> = animalDao.getAllUpcomingVaccinations()
    val allCampAlerts: Flow<List<CampAlert>> = animalDao.getAllCampAlerts()

    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>> {
        return animalDao.getVaccinationsForAnimal(animalId)
    }

    // ─── Language ─────────────────────────────────────────────────────────────────────
    // BUG 4 FIX: Load saved language from SharedPreferences so it persists across restarts
    private val _currentLanguage = MutableStateFlow(
        prefs.getString("lang", "en") ?: "en"
    )
    val currentLanguage = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == "en") "kn" else "en"
        _currentLanguage.value = next
        // Save to SharedPreferences — survives app restart
        prefs.edit().putString("lang", next).apply()
        // NOTE: The Activity calls recreate() after this — see DashboardScreen
    }

    // ─── Camp Alert Registration ───────────────────────────────────────────────────────
    // BUG 3 FIX: Load saved registrations from SharedPreferences on startup
    private fun loadRegisteredAlerts(): Set<Int> {
        val saved = prefs.getStringSet("registered_alert_ids", emptySet()) ?: emptySet()
        return saved.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private val _registeredAlertIds = MutableStateFlow<Set<Int>>(loadRegisteredAlerts())
    val registeredAlertIds = _registeredAlertIds.asStateFlow()

    fun toggleAlertRegistration(alertId: Int) {
        val updated = if (_registeredAlertIds.value.contains(alertId)) {
            _registeredAlertIds.value - alertId
        } else {
            _registeredAlertIds.value + alertId
        }
        _registeredAlertIds.value = updated
        // BUG 3 FIX: Save updated set to SharedPreferences
        prefs.edit()
            .putStringSet("registered_alert_ids", updated.map { it.toString() }.toSet())
            .apply()
    }

    // BUG 10 FIX: Allow users to add new camp alerts from the UI
    fun addCampAlert(title: String, description: String, location: String, date: Long, type: String) {
        viewModelScope.launch {
            animalDao.insertCampAlert(
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

    // ─── Add Animal ────────────────────────────────────────────────────────────────────
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
                name = name, species = species, breed = breed, gender = gender,
                ageInYears = ageInYears, district = district, notes = notes, photoUri = photoUri
            )
            val id = animalDao.insertAnimal(newAnimal).toInt()

            lastVaccineDates.forEach { (vaccineName, lastDate) ->
                if (lastDate != null) {
                    val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365
                    val nextDate = nextVaccineDates[vaccineName]
                        ?: (lastDate + (intervalDays * 24L * 60 * 60 * 1000))
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id, vaccineName = vaccineName,
                            dateAdministered = lastDate, nextDueDate = nextDate, isCompleted = true
                        )
                    )
                }
            }

            nextVaccineDates.forEach { (vaccineName, nextDate) ->
                if (nextDate != null && !lastVaccineDates.containsKey(vaccineName)) {
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id, vaccineName = vaccineName,
                            dateAdministered = 0, nextDueDate = nextDate, isCompleted = false
                        )
                    )
                }
            }

            VaccineConstants.HOTSPOT_ZONES.forEach { (vaccine, districts) ->
                if (districts.contains(district) && !lastVaccineDates.containsKey(vaccine)) {
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id, vaccineName = vaccine,
                            dateAdministered = 0,
                            nextDueDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L),
                            isCompleted = false
                        )
                    )
                }
            }

            if (lastVaccineDates.isEmpty()) {
                animalDao.insertVaccination(
                    Vaccination(
                        animalId = id, vaccineName = "Initial Health Checkup",
                        dateAdministered = System.currentTimeMillis(),
                        nextDueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                        isCompleted = false
                    )
                )
            }

            scheduleVaccineReminder()
        }
    }

    // ─── Update Animal ─────────────────────────────────────────────────────────────────
    fun updateAnimal(
        id: Int, name: String, species: String, breed: String,
        gender: String, ageInYears: Int, district: String, notes: String,
        photoUri: String?, lastVaccineDates: Map<String, Long?>, nextVaccineDates: Map<String, Long?>
    ) {
        viewModelScope.launch {
            val updatedAnimal = Animal(
                id = id, name = name, species = species, breed = breed,
                gender = gender, ageInYears = ageInYears, district = district,
                notes = notes, photoUri = photoUri
            )
            animalDao.updateAnimal(updatedAnimal)

            // BUG 1 FIX: Only delete & re-insert the SPECIFIC vaccine being submitted.
            // Old code wiped ALL vaccination records for the animal on every edit.
            // Now we only remove the one vaccine that the user is updating, leaving
            // all other historical records intact.
            lastVaccineDates.forEach { (vaccineName, lastDate) ->
                // Delete only this specific vaccine's old record (not everything)
                animalDao.deleteVaccinationsForAnimalAndVaccine(id, vaccineName)

                if (lastDate != null) {
                    val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365
                    val nextDate = nextVaccineDates[vaccineName]
                        ?: (lastDate + (intervalDays * 24L * 60 * 60 * 1000))
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id, vaccineName = vaccineName,
                            dateAdministered = lastDate, nextDueDate = nextDate, isCompleted = true
                        )
                    )
                }
            }

            nextVaccineDates.forEach { (vaccineName, nextDate) ->
                if (nextDate != null && !lastVaccineDates.containsKey(vaccineName)) {
                    // Delete old pending record for this vaccine before inserting new one
                    animalDao.deleteVaccinationsForAnimalAndVaccine(id, vaccineName)
                    animalDao.insertVaccination(
                        Vaccination(
                            animalId = id, vaccineName = vaccineName,
                            dateAdministered = 0, nextDueDate = nextDate, isCompleted = false
                        )
                    )
                }
            }

            scheduleVaccineReminder()
        }
    }

    private fun scheduleVaccineReminder() {
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
        viewModelScope.launch { animalDao.deleteAnimal(animal) }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val count = animalDao.getAnimalCount()
            if (count > 0) return@launch

            val sheepId = animalDao.insertAnimal(
                Animal(name = "Muttu", species = "Sheep", breed = "Deccani", gender = "Male", ageInYears = 1, notes = "Healthy ram")
            ).toInt()
            val goatId = animalDao.insertAnimal(
                Animal(name = "Gauri", species = "Goat", breed = "Osmanabadi", gender = "Female", ageInYears = 2, notes = "Due for PPR")
            ).toInt()

            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L

            animalDao.insertVaccination(Vaccination(
                animalId = sheepId, vaccineName = VaccineConstants.FMD,
                dateAdministered = now - (30 * day), nextDueDate = now + (150 * day), isCompleted = true
            ))
            animalDao.insertVaccination(Vaccination(
                animalId = goatId, vaccineName = VaccineConstants.PPR,
                dateAdministered = now - (400 * day), nextDueDate = now - (35 * day), isCompleted = false
            ))

            val calendar = Calendar.getInstance()

            calendar.set(Calendar.MONTH, Calendar.APRIL)
            calendar.set(Calendar.DAY_OF_MONTH, 15)
            animalDao.insertCampAlert(CampAlert(
                title = "FMD Vaccination Camp",
                description = "Government organized FMD vaccination drive for all livestock in the village.",
                location = "Grama Panchayat Office",
                date = calendar.timeInMillis,
                type = "Vaccination Camp"
            ))

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

class AnimalViewModelFactory(
    private val application: Application,
    private val animalDao: AnimalDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnimalViewModel(application, animalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}