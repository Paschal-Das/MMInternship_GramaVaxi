package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.*
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(animalId: Int, navController: NavHostController, viewModel: AnimalViewModel) {
    val animals by viewModel.allAnimals.collectAsState(initial = emptyList())
    val animal = animals.find { it.id == animalId }
    val animalVaccines by viewModel.getVaccinationsForAnimal(animalId).collectAsState(initial = emptyList())

    val pastVaccines = animalVaccines.filter { it.isCompleted }.sortedByDescending { it.dateAdministered }
    val futureVaccines = animalVaccines.filter { !it.isCompleted }.sortedBy { it.nextDueDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(animal?.name ?: stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    animal?.let {
                        IconButton(onClick = { navController.navigate("edit_animal/${it.id}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        animal?.let {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── PHOTO DISPLAY ─────────────────────────────────────────────
                // Display animal photo if available (FIX #3)
                item {
                    if (animal.photoUri != null) {
                        Text(
                            text = stringResource(R.string.animal_photo),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        AsyncImage(
                            model = animal.photoUri,
                            contentDescription = stringResource(R.string.animal_photo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                item { AnimalInfoCard(it) }

                item {
                    Text(
                        text = stringResource(R.string.upcoming_doses),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (futureVaccines.isEmpty()) {
                    item { Text(stringResource(R.string.no_upcoming), color = MaterialTheme.colorScheme.outline) }
                } else {
                    items(futureVaccines) { vacc -> VaccineItem(vacc, isPast = false) }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.vaccination_history),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (pastVaccines.isEmpty()) {
                    item { Text(stringResource(R.string.no_history), color = MaterialTheme.colorScheme.outline) }
                } else {
                    items(pastVaccines) { vacc -> VaccineItem(vacc, isPast = true) }
                }
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun AnimalInfoCard(animal: Animal) {
    val speciesText = if (animal.species == "Sheep") stringResource(R.string.sheep) else stringResource(R.string.goat)
    val genderText = if (animal.gender == "Male") stringResource(R.string.male) else stringResource(R.string.female)
    val breedResId = (AnimalConstants.SHEEP_BREEDS + AnimalConstants.GOAT_BREEDS)
        .find { it.first == animal.breed }?.second
    val breedText = if (breedResId != null) stringResource(breedResId) else animal.breed

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            animal.photoUri?.let { uri ->
                AsyncImage(
                    model = uri, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${stringResource(R.string.breed)}: $breedText", style = MaterialTheme.typography.bodyLarge)
                    Text("${stringResource(R.string.species)}: $speciesText", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${stringResource(R.string.gender)}: $genderText", style = MaterialTheme.typography.bodyMedium)
                    Text("${stringResource(R.string.age_years)}: ${animal.ageInYears}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (animal.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.notes_description), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(animal.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun VaccineItem(vaccination: Vaccination, isPast: Boolean) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val dateText = if (isPast) {
        // ✅ Bug 6 fix: dateAdministered = 0L means never actually given — show dash instead of "01 Jan 1970"
        if (vaccination.dateAdministered == 0L) "—"
        else sdf.format(Date(vaccination.dateAdministered))
    } else {
        vaccination.nextDueDate?.let { sdf.format(Date(it)) } ?: "TBD"
    }

    val vaccineResId = when (vaccination.vaccineName) {
        VaccineConstants.FMD -> R.string.fmd_vaccine
        VaccineConstants.PPR -> R.string.ppr_vaccine
        VaccineConstants.POX -> R.string.pox_vaccine
        VaccineConstants.HS -> R.string.hs_vaccine
        VaccineConstants.BLUETONGUE -> R.string.bluetongue_vaccine
        VaccineConstants.ENTEROTOXEMIA -> R.string.enterotoxemia
        VaccineConstants.CCPP -> R.string.ccpp_vaccine
        VaccineConstants.ANTHRAX -> R.string.anthrax_vaccine
        "Initial Health Checkup" -> R.string.initial_checkup
        else -> null
    }
    val vaccineDisplayName = if (vaccineResId != null) stringResource(vaccineResId) else vaccination.vaccineName

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPast) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                contentDescription = null,
                tint = if (isPast) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = vaccineDisplayName, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isPast) stringResource(R.string.given_on, dateText)
                    else stringResource(R.string.due_on, dateText),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}