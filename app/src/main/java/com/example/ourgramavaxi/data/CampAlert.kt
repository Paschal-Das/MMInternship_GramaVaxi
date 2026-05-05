package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camp_alerts")
data class CampAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,           // ✅ Was: titleResId: Int
    val description: String,     // ✅ Was: descResId: Int
    val location: String,        // ✅ Was: locationResId: Int
    val date: Long,
    val type: String             // ✅ Was: typeResId: Int
)