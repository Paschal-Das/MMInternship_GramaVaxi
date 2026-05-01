package com.example.ourgramavaxi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ourgramavaxi.ui.screens.*
import com.example.ourgramavaxi.ui.theme.OurGramaVaxiTheme
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import com.example.ourgramavaxi.viewmodel.AnimalViewModelFactory
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as GramaVaxiApplication
            val viewModel: AnimalViewModel = viewModel(
                factory = AnimalViewModelFactory(app, app.database.animalDao())
            )
            
            LaunchedEffect(Unit) {
                viewModel.seedSampleData()
            }

            val currentLang by viewModel.currentLanguage.collectAsState()

            // Update locale dynamically
            CompositionLocalProvider(androidx.compose.ui.platform.LocalContext provides updateLocale(LocalContext.current, currentLang)) {
                OurGramaVaxiTheme {
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") { DashboardScreen(navController, viewModel) }
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
                    }
                }
            }
        }
    }

    private fun updateLocale(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
