package com.example.ourgramavaxi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.data.AnimalDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AnimalViewModel(private val animalDao: AnimalDao) : ViewModel() {

    val allAnimals: Flow<List<Animal>> = animalDao.getAllAnimals()

    fun addAnimal(name: String, breed: String, age: Int) {
        viewModelScope.launch {
            val newAnimal = Animal(
                name = name,
                breed = breed,
                age = age
            )
            animalDao.insertAnimal(newAnimal)
        }
    }

    fun deleteAnimal(animal: Animal) {
        viewModelScope.launch {
            animalDao.deleteAnimal(animal)
        }
    }
}

class AnimalViewModelFactory(private val animalDao: AnimalDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnimalViewModel(animalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
