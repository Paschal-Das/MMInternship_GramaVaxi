package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.viewmodel.AnimalViewModel

import com.example.ourgramavaxi.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalLedgerScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    val animals by viewModel.allAnimals.collectAsState(initial = emptyList())
    val upcomingVaccinations by viewModel.allUpcomingVaccinations.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.animal_ledger)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_animal") }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_animal))
            }
        }
    ) { innerPadding ->
        if (animals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_animals),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
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
                    val isDue = upcomingVaccinations.any { 
                        it.animalId == animal.id && (it.nextDueDate ?: 0L) < (System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L) && !it.isCompleted
                    }
                    AnimalItem(
                        animal = animal, 
                        isDue = isDue,
                        onDelete = { viewModel.deleteAnimal(animal) },
                        onClick = { navController.navigate("animal_detail/${animal.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimalItem(animal: Animal, isDue: Boolean, onDelete: () -> Unit, onClick: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_animal)) },
            text = { Text(stringResource(R.string.delete_confirm, animal.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (animal.species == "Goat") Icons.Default.Agriculture else Icons.Default.Pets
            val speciesText = if (animal.species == "Goat") stringResource(R.string.goat) else stringResource(R.string.sheep)
            
            // Look up breed translation
            val breedResId = (AnimalConstants.SHEEP_BREEDS + AnimalConstants.GOAT_BREEDS)
                .find { it.first == animal.breed }?.second
            val breedText = if (breedResId != null) stringResource(breedResId) else animal.breed

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (animal.photoUri != null) {
                        AsyncImage(
                            model = animal.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = animal.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (isDue) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                            Text(stringResource(R.string.due), modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
                Text(
                    text = "$speciesText • $breedText • ${animal.ageInYears} ${stringResource(R.string.age_years)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
