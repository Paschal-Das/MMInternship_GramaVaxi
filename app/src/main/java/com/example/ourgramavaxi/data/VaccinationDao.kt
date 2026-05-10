package com.example.ourgramavaxi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: Vaccination)

    @Query("SELECT * FROM vaccinations WHERE animalId = :animalId ORDER BY dateAdministered DESC")
    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE isCompleted = 0 AND nextDueDate IS NOT NULL ORDER BY nextDueDate ASC")
    fun getAllUpcomingVaccinations(): Flow<List<Vaccination>>

    @Query("DELETE FROM vaccinations WHERE animalId = :animalId AND vaccineName = :vaccineName")
    suspend fun deleteSpecificVaccination(animalId: Int, vaccineName: String)

    @Query("SELECT COUNT(*) FROM vaccinations WHERE animalId = :animalId AND isCompleted = 0")
    fun getPendingVaccinationCount(animalId: Int): Flow<Int>
}