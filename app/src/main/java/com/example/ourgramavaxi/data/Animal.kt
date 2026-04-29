package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val breed: String,
    val age: Int, // Age in months
    val photoUri: String? = null,
    val lastVaccinationDate: Long? = null // Timestamp
)
