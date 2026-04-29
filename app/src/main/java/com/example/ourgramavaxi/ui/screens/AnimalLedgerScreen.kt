package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.viewmodel.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalLedgerScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    val animals by viewModel.allAnimals.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animal Ledger (ಪ್ರಾಣಿಗಳ ಪಟ್ಟಿ)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_animal") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Animal")
            }
        }
    ) { innerPadding ->
        if (animals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No animals registered yet.\nTap + to add one.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(animals) { animal ->
                    AnimalItem(animal)
                }
            }
        }
    }
}

@Composable
fun AnimalItem(animal: Animal) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = animal.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Breed: ${animal.breed} | Age: ${animal.age} months")
            }
        }
    }
}
