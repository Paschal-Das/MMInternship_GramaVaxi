package com.example.ourgramavaxi.domain.usecase

import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.data.Vaccination
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.repository.AnimalRepository
import javax.inject.Inject

class UpdateAnimalUseCase @Inject constructor(
    private val repository: AnimalRepository
) {
    suspend operator fun invoke(
        id: Int,
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageInYears: Int,
        district: String,
        notes: String,
        photoUri: String?,
        lastVaccineDates: Map<String, Long?>,
        nextVaccineDates: Map<String, Long?>
    ) {
        val updatedAnimal = Animal(
            id = id,
            name = name,
            species = species,
            breed = breed,
            gender = gender,
            ageInYears = ageInYears,
            district = district,
            notes = notes,
            photoUri = photoUri
        )
        repository.updateAnimal(updatedAnimal)

        // ── LOOP 1: Re-insert vaccines where farmer provided a "Last Given" date ──
        lastVaccineDates.forEach { (vaccineName, lastDate) ->
            // Delete ALL existing records for this vaccine (both completed + upcoming)
            // before re-inserting, so edits don't create duplicate rows.
            repository.deleteVaccination(id, vaccineName)

            if (lastDate != null) {
                val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365

                // ✅ BUG B1 FIX: Same two-record split as RegisterAnimalUseCase.
                // History record
                repository.insertVaccination(
                    Vaccination(
                        animalId = id,
                        vaccineName = vaccineName,
                        dateAdministered = lastDate,
                        nextDueDate = null,
                        isCompleted = true
                    )
                )
                // Upcoming reminder record
                val nextDate = nextVaccineDates[vaccineName]
                    ?: (lastDate + (intervalDays * 24L * 60 * 60 * 1000))
                repository.insertVaccination(
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

        // ── LOOP 2: Handle next-only vaccinations ──
        nextVaccineDates.forEach { (vaccineName, nextDate) ->
            // ✅ BUG B2 FIX: was `!lastVaccineDates.containsKey(vaccineName)`
            if (nextDate != null && lastVaccineDates[vaccineName] == null) {
                repository.deleteVaccination(id, vaccineName)
                repository.insertVaccination(
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
    }
}