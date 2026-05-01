package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camp_alerts")
data class CampAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val location: String,
    val date: Long,
    val type: String // e.g., "Vaccination Camp", "Health Checkup"
)
