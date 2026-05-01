package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "vaccinations",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Vaccination(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: Int,
    val vaccineName: String,
    val dateAdministered: Long,
    val nextDueDate: Long? = null,
    val isCompleted: Boolean = true
)
