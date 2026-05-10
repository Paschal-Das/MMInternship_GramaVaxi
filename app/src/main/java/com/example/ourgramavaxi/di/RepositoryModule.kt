package com.example.ourgramavaxi.di

import android.content.Context
import com.example.ourgramavaxi.data.AnimalDao
import com.example.ourgramavaxi.data.CampAlertDao
import com.example.ourgramavaxi.data.VaccinationDao
import com.example.ourgramavaxi.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAnimalRepository(
        animalDao: AnimalDao,
        vaccinationDao: VaccinationDao,
        campAlertDao: CampAlertDao
    ): AnimalRepository {
        return AnimalRepositoryImpl(animalDao, vaccinationDao, campAlertDao)
    }

    @Provides
    @Singleton
    fun providePreferenceRepository(
        @ApplicationContext context: Context
    ): PreferenceRepository {
        return PreferenceRepositoryImpl(context)
    }
}
