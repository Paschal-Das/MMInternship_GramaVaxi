package com.example.ourgramavaxi.di

import android.content.Context
import androidx.room.Room
import com.example.ourgramavaxi.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "grama_vaxi_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAnimalDao(database: AppDatabase) = database.animalDao()

    @Provides
    @Singleton
    fun provideVaccinationDao(database: AppDatabase) = database.vaccinationDao()

    @Provides
    @Singleton
    fun provideCampAlertDao(database: AppDatabase) = database.campAlertDao()
}