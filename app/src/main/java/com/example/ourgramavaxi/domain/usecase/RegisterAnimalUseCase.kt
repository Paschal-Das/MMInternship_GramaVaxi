package com.example.ourgramavaxi.domain.usecase

import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.data.Vaccination
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.repository.AnimalRepository
import javax.inject.Inject

class RegisterAnimalUseCase @Inject constructor(
    private val repository: AnimalRepository
) {
    suspend operator fun invoke(
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
    ): Long {
        val animalId = repository.insertAnimal(
            Animal(
                name = name,
                species = species,
                breed = breed,
                gender = gender,
                ageInYears = ageInYears,
                district = district,
                notes = notes,
                photoUri = photoUri
            )
        ).toInt()

        // Handle Last Vaccinations
        lastVaccineDates.forEach { (vaccineName, lastDate) ->
            if (lastDate != null) {
                val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365
                val nextDate = nextVaccineDates[vaccineName]
                    ?: (lastDate + (intervalDays * 24L * 60 * 60 * 1000))
                repository.insertVaccination(
                    Vaccination(
                        animalId = animalId,
                        vaccineName = vaccineName,
                        dateAdministered = lastDate,
                        nextDueDate = nextDate,
                        isCompleted = true
                    )
                )
            }
        }

        // Handle Next Scheduled Vaccinations (if not already handled by lastVaccineDates)
        nextVaccineDates.forEach { (vaccineName, nextDate) ->
            if (nextDate != null && !lastVaccineDates.containsKey(vaccineName)) {
                repository.insertVaccination(
                    Vaccination(
                        animalId = animalId,
                        vaccineName = vaccineName,
                        dateAdministered = 0,
                        nextDueDate = nextDate,
                        isCompleted = false
                    )
                )
            }
        }

        // Handle Hotspot Zones
        VaccineConstants.HOTSPOT_ZONES.forEach { (vaccine, districts) ->
            if (districts.contains(district) && !lastVaccineDates.containsKey(vaccine)) {
                repository.insertVaccination(
                    Vaccination(
                        animalId = animalId,
                        vaccineName = vaccine,
                        dateAdministered = 0,
                        nextDueDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L),
                        isCompleted = false
                    )
                )
            }
        }

        // Initial Health Checkup if no vaccines provided
        if (lastVaccineDates.isEmpty() && nextVaccineDates.isEmpty()) {
            repository.insertVaccination(
                Vaccination(
                    animalId = animalId,
                    vaccineName = "Initial Health Checkup",
                    dateAdministered = System.currentTimeMillis(),
                    nextDueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                    isCompleted = false
                )
            )
        }

        return animalId.toLong()
    }
}