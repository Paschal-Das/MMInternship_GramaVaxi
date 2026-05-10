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

        // Only delete & re-insert the SPECIFIC vaccine being submitted.
        lastVaccineDates.forEach { (vaccineName, lastDate) ->
            repository.deleteVaccination(id, vaccineName)

            if (lastDate != null) {
                val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365
                val nextDate = nextVaccineDates[vaccineName]
                    ?: (lastDate + (intervalDays * 24L * 60 * 60 * 1000))
                repository.insertVaccination(
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

        nextVaccineDates.forEach { (vaccineName, nextDate) ->
            if (nextDate != null && !lastVaccineDates.containsKey(vaccineName)) {
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
