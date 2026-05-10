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

        // ── LOOP 1: Process vaccines where the farmer provided a "Last Given" date ──
        lastVaccineDates.forEach { (vaccineName, lastDate) ->
            if (lastDate != null) {
                val intervalDays = VaccineConstants.VACCINE_INTERVALS[vaccineName] ?: 365

                // ✅ BUG B1 FIX: Insert TWO records instead of one combined record.
                //
                // OLD CODE created one record: isCompleted=TRUE with nextDueDate set.
                // Problem: getAllUpcomingVaccinations() filters "WHERE isCompleted = 0",
                // so the nextDueDate was permanently invisible to the Calendar and Worker.
                //
                // NEW CODE: Split into:
                //   Record 1 (history) — isCompleted=true, shows in "Vaccination History" tab
                //   Record 2 (upcoming) — isCompleted=false, shows in Calendar, triggers notifications

                // Record 1: Historical entry — "This vaccine was given on [date]"
                repository.insertVaccination(
                    Vaccination(
                        animalId = animalId,
                        vaccineName = vaccineName,
                        dateAdministered = lastDate,
                        nextDueDate = null,   // History record doesn't need a nextDueDate
                        isCompleted = true
                    )
                )

                // Record 2: Upcoming reminder — "Next dose is due on [date]"
                val nextDate = nextVaccineDates[vaccineName]
                    ?: (lastDate + (intervalDays * 24L * 60 * 60 * 1000))
                repository.insertVaccination(
                    Vaccination(
                        animalId = animalId,
                        vaccineName = vaccineName,
                        dateAdministered = 0,
                        nextDueDate = nextDate,
                        isCompleted = false   // ← This is what the Calendar and Worker read
                    )
                )
            }
        }

        // ── LOOP 2: Process vaccines where ONLY a "Next Due" date was provided ──
        nextVaccineDates.forEach { (vaccineName, nextDate) ->
            // ✅ BUG B2 FIX: was `!lastVaccineDates.containsKey(vaccineName)`
            // That check returns FALSE when the key exists with a null value (farmer
            // selected a vaccine but didn't pick a last date). The null-value key
            // tricked the check into skipping this whole block, silently losing the date.
            // Fix: check the VALUE is null, not whether the key is absent.
            if (nextDate != null && lastVaccineDates[vaccineName] == null) {
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

        // ── Hotspot Zone Auto-Alert ──
        VaccineConstants.HOTSPOT_ZONES.forEach { (vaccine, districts) ->
            if (districts.contains(district) && lastVaccineDates[vaccine] == null) {
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

        // ── Default Health Checkup (when farmer entered no vaccine info at all) ──
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