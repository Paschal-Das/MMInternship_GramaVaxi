package com.example.ourgramavaxi.repository

import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.data.Vaccination
import com.example.ourgramavaxi.data.CampAlert
import kotlinx.coroutines.flow.Flow

interface AnimalRepository {
    // Animals
    fun getAllAnimals(): Flow<List<Animal>>
    suspend fun insertAnimal(animal: Animal): Long
    suspend fun updateAnimal(animal: Animal)
    suspend fun deleteAnimal(animal: Animal)
    suspend fun getAnimalById(id: Int): Animal?
    fun getAnimalCount(): Flow<Int>
    fun searchAnimals(query: String): Flow<List<Animal>>

    // Vaccinations
    fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>>
    fun getAllUpcomingVaccinations(): Flow<List<Vaccination>>
    suspend fun insertVaccination(vaccination: Vaccination)
    suspend fun deleteVaccination(animalId: Int, vaccineName: String)
    fun getPendingVaccinationCount(animalId: Int): Flow<Int>

    // Camp Alerts
    fun getAllCampAlerts(): Flow<List<CampAlert>>
    fun getUpcomingCamps(): Flow<List<CampAlert>>
    suspend fun insertCampAlert(campAlert: CampAlert)
    suspend fun deleteCampAlert(campAlert: CampAlert)
}