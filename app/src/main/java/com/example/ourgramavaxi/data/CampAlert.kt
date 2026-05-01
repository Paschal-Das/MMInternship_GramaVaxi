package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camp_alerts")
data class CampAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleResId: Int,
    val descResId: Int,
    val locationResId: Int,
    val date: Long,
    val typeResId: Int
)
