package com.example.ourgramavaxi.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val context: Context
) : PreferenceRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("grama_vaxi_prefs", Context.MODE_PRIVATE)

    override val currentLanguage: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "lang") {
                trySend(p.getString("lang", "en") ?: "en")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString("lang", "en") ?: "en")
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setLanguage(lang: String) {
        prefs.edit().putString("lang", lang).apply()
    }

    override val registeredAlertIds: Flow<Set<Int>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "registered_alert_ids") {
                val saved = p.getStringSet("registered_alert_ids", emptySet()) ?: emptySet()
                trySend(saved.mapNotNull { it.toIntOrNull() }.toSet())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        val initial = prefs.getStringSet("registered_alert_ids", emptySet()) ?: emptySet()
        trySend(initial.mapNotNull { it.toIntOrNull() }.toSet())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun toggleAlertRegistration(alertId: Int) {
        val current = prefs.getStringSet("registered_alert_ids", emptySet()) ?: emptySet()
        val updated = if (current.contains(alertId.toString())) {
            current - alertId.toString()
        } else {
            current + alertId.toString()
        }
        prefs.edit().putStringSet("registered_alert_ids", updated).apply()
    }
}
