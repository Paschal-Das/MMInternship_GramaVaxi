package com.example.ourgramavaxi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ourgramavaxi.ui.screens.AddAnimalScreen
import com.example.ourgramavaxi.ui.screens.AnimalLedgerScreen
import com.example.ourgramavaxi.ui.screens.DashboardScreen
import com.example.ourgramavaxi.ui.theme.OurGramaVaxiTheme
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import com.example.ourgramavaxi.viewmodel.AnimalViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OurGramaVaxiTheme {
                val navController = rememberNavController()
                val app = application as GramaVaxiApplication
                val viewModel: AnimalViewModel = viewModel(
                    factory = AnimalViewModelFactory(app.database.animalDao())
                )

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") { DashboardScreen(navController) }
                    composable("ledger") { AnimalLedgerScreen(navController, viewModel) }
                    composable("add_animal") { AddAnimalScreen(navController, viewModel) }
                }
            }
        }
    }
}
