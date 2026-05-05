package com.example.ourgramavaxi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals ORDER BY id DESC")
    fun getAllAnimals(): Flow<List<Animal>>

    @Query("SELECT COUNT(*) FROM animals")
    suspend fun getAnimalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal): Long

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    @Update
    suspend fun updateAnimal(animal: Animal)

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: Int): Animal?

    // Vaccinations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: Vaccination)

    @Query("SELECT * FROM vaccinations WHERE animalId = :animalId ORDER BY dateAdministered DESC")
    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE isCompleted = 0 AND nextDueDate IS NOT NULL ORDER BY nextDueDate ASC")
    fun getAllUpcomingVaccinations(): Flow<List<Vaccination>>

    // BUG 1 FIX: Delete ALL vaccinations for an animal (used only when needed carefully)
    @Query("DELETE FROM vaccinations WHERE animalId = :animalId")
    suspend fun deleteVaccinationsForAnimal(animalId: Int)

    // BUG 1 FIX (NEW): Delete only ONE specific vaccine for an animal.
    // This lets updateAnimal() replace only the vaccine being edited,
    // without wiping the rest of the animal's vaccination history.
    @Query("DELETE FROM vaccinations WHERE animalId = :animalId AND vaccineName = :vaccineName")
    suspend fun deleteVaccinationsForAnimalAndVaccine(animalId: Int, vaccineName: String)

    // Camp Alerts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampAlert(campAlert: CampAlert)

    @Query("SELECT * FROM camp_alerts ORDER BY date ASC")
    fun getAllCampAlerts(): Flow<List<CampAlert>>

    @Delete
    suspend fun deleteCampAlert(campAlert: CampAlert)
}