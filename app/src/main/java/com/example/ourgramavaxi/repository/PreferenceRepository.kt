package com.example.ourgramavaxi.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val currentLanguage: Flow<String>
    suspend fun setLanguage(lang: String)

    val registeredAlertIds: Flow<Set<Int>>
    suspend fun toggleAlertRegistration(alertId: Int)
}
