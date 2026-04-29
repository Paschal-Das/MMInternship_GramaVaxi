package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.viewmodel.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Animal (ಪ್ರಾಣಿ ನೋಂದಣಿ)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Animal Name/Tag (ಹೆಸರು/ಟ್ಯಾಗ್)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed (ತಳಿ - Sheep/Goat)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age in Months (ವಯಸ್ಸು ತಿಂಗಳುಗಳಲ್ಲಿ)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank() && breed.isNotBlank()) {
                        viewModel.addAnimal(name, breed, age.toIntOrNull() ?: 0)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Register (ನೋಂದಣಿ ಮಾಡಿ)")
            }
        }
    }
}
