package com.example.ourgramavaxi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals ORDER BY id DESC")
    fun getAllAnimals(): Flow<List<Animal>>

    @Query("SELECT COUNT(*) FROM animals")
    suspend fun getAnimalCount(): Int

    @Query("SELECT COUNT(*) FROM animals")
    fun getAnimalCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal): Long

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    @Update
    suspend fun updateAnimal(animal: Animal)

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: Int): Animal?

    @Query("SELECT * FROM animals WHERE name LIKE :query")
    fun searchAnimals(query: String): Flow<List<Animal>>
}