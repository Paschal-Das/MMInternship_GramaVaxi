package com.example.ourgramavaxi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals ORDER BY id DESC")
    fun getAllAnimals(): Flow<List<Animal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal): Long

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: Int): Animal?

    // Vaccinations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: Vaccination)

    @Query("SELECT * FROM vaccinations WHERE animalId = :animalId ORDER BY dateAdministered DESC")
    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations ORDER BY nextDueDate ASC")
    fun getAllUpcomingVaccinations(): Flow<List<Vaccination>>

    // Camp Alerts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampAlert(campAlert: CampAlert)

    @Query("SELECT * FROM camp_alerts ORDER BY date ASC")
    fun getAllCampAlerts(): Flow<List<CampAlert>>

    @Delete
    suspend fun deleteCampAlert(campAlert: CampAlert)
}
