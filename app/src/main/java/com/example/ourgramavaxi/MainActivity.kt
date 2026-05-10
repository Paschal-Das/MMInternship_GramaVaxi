package com.example.ourgramavaxi

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ourgramavaxi.ui.screens.*
import com.example.ourgramavaxi.ui.theme.OurGramaVaxiTheme
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import java.util.*

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AnimalViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                viewModel.seedSampleData()
            }

            // KANNADA FIX: We no longer use CompositionLocalProvider to swap locale.
            // Instead, we use attachBaseContext() below to apply the correct locale
            // BEFORE the Activity starts, and call recreate() when the user toggles.
            // This is the correct Android way — the system picks up strings.xml from
            // values-kn/ automatically when the locale is set at the context level.
            OurGramaVaxiTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            navController = navController,
                            viewModel = viewModel,
                            // Pass recreate lambda so DashboardScreen can trigger it
                            // after language toggle without needing a direct Activity reference
                            onLanguageToggle = {
                                viewModel.toggleLanguage()
                                recreate() // Restart activity to apply new locale
                            }
                        )
                    }
                    composable("animal_ledger") { AnimalLedgerScreen(navController, viewModel) }
                    composable("add_animal") { AddAnimalScreen(navController, viewModel) }
                    composable("edit_animal/{animalId}") { backStackEntry ->
                        val animalId = backStackEntry.arguments?.getString("animalId")?.toIntOrNull()
                        AddAnimalScreen(navController, viewModel, animalId)
                    }
                    composable("animal_detail/{animalId}") { backStackEntry ->
                        val animalId = backStackEntry.arguments?.getString("animalId")?.toIntOrNull() ?: 0
                        AnimalDetailScreen(animalId, navController, viewModel)
                    }
                    composable("report_sick") { ReportSickScreen(navController, viewModel) }
                    composable("vaccine_calendar") { VaccineCalendarScreen(navController, viewModel) }
                    composable("camp_alerts") { CampAlertsScreen(navController, viewModel) }
                    composable("general_information") { GeneralInformationScreen(navController) }
                }
            }
        }
    }

    // KANNADA FIX (KEY METHOD): This runs before setContent(), so the correct locale
    // is already applied when any composable reads stringResource().
    // It reads the saved language from SharedPreferences and wraps the base context
    // with a locale-configured context. When recreate() is called after toggle,
    // this method runs again with the new language — all strings refresh automatically.
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("grama_vaxi_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}