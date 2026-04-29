package com.example.ourgramavaxi

import android.app.Application
import com.example.ourgramavaxi.data.AppDatabase

class GramaVaxiApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
