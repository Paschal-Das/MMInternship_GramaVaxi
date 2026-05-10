package com.example.ourgramavaxi.repository

import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.data.AnimalDao
import com.example.ourgramavaxi.data.CampAlert
import com.example.ourgramavaxi.data.CampAlertDao
import com.example.ourgramavaxi.data.Vaccination
import com.example.ourgramavaxi.data.VaccinationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnimalRepositoryImpl @Inject constructor(
    private val animalDao: AnimalDao,
    private val vaccinationDao: VaccinationDao,
    private val campAlertDao: CampAlertDao
) : AnimalRepository {

    override fun getAllAnimals(): Flow<List<Animal>> = animalDao.getAllAnimals()

    override suspend fun insertAnimal(animal: Animal): Long = animalDao.insertAnimal(animal)

    override suspend fun updateAnimal(animal: Animal) = animalDao.updateAnimal(animal)

    override suspend fun deleteAnimal(animal: Animal) = animalDao.deleteAnimal(animal)

    override suspend fun getAnimalById(id: Int): Animal? = animalDao.getAnimalById(id)

    override fun getAnimalCount(): Flow<Int> {
        return animalDao.getAnimalCountFlow()
    }

    override fun searchAnimals(query: String): Flow<List<Animal>> = animalDao.searchAnimals(query)

    override fun getVaccinationsForAnimal(animalId: Int): Flow<List<Vaccination>> =
        vaccinationDao.getVaccinationsForAnimal(animalId)

    override fun getAllUpcomingVaccinations(): Flow<List<Vaccination>> =
        vaccinationDao.getAllUpcomingVaccinations()

    override suspend fun insertVaccination(vaccination: Vaccination) =
        vaccinationDao.insertVaccination(vaccination)

    override suspend fun deleteVaccination(animalId: Int, vaccineName: String) =
        vaccinationDao.deleteSpecificVaccination(animalId, vaccineName)

    override fun getPendingVaccinationCount(animalId: Int): Flow<Int> =
        vaccinationDao.getPendingVaccinationCount(animalId)

    override fun getAllCampAlerts(): Flow<List<CampAlert>> = campAlertDao.getAllCampAlerts()

    override fun getUpcomingCamps(): Flow<List<CampAlert>> {
        return campAlertDao.getUpcomingCamps(System.currentTimeMillis())
    }

    override suspend fun insertCampAlert(campAlert: CampAlert) =
        campAlertDao.insertCampAlert(campAlert)

    override suspend fun deleteCampAlert(campAlert: CampAlert) =
        campAlertDao.deleteCampAlert(campAlert)
}