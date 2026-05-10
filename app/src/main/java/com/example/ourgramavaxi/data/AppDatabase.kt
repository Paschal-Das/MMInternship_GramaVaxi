package com.example.ourgramavaxi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// The database is created and managed exclusively by Hilt via DatabaseModule.kt.
// Having two creation paths risks creating two separate databases which causes data loss.
@Database(
    entities = [Animal::class, Vaccination::class, CampAlert::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun campAlertDao(): CampAlertDao
}
@Database(
    entities = [Animal::class, Vaccination::class, CampAlert::class],
    version = 7,  // Bumped version for new DAOs
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun vaccinationDao(): VaccinationDao  // NEW
    abstract fun campAlertDao(): CampAlertDao      // NEW

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grama_vaxi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}