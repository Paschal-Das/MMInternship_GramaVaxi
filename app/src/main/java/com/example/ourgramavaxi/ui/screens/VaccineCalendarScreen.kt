package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineCalendarScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    val vaccinations by viewModel.allUpcomingVaccinations.collectAsState(initial = emptyList())
    val animals by viewModel.allAnimals.collectAsState(initial = emptyList())
    
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Active Campaigns for the current month in Karnataka
    val activeCampaigns = VaccineConstants.SEASONAL_WINDOWS.filter { it.value.contains(currentMonth) }.keys.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vaccine_calendar)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campaign Alert Section
            if (activeCampaigns.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active Campaign Window (ಸಕ್ರಿಯ ಲಸಿಕೆ ಅಭಿಯಾನ)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Karnataka Govt. is currently prioritizing: ${activeCampaigns.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Scheduled Vaccines
            item {
                Text(
                    text = "Your Animal Schedule (ನಿಮ್ಮ ವೇಳಾಪಟ್ಟಿ)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val upcoming = vaccinations.filter { !it.isCompleted }.sortedBy { it.nextDueDate }

            if (upcoming.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No vaccinations scheduled.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(upcoming) { vacc ->
                    val animal = animals.find { it.id == vacc.animalId }
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = vacc.vaccineName, fontWeight = FontWeight.Bold)
                                Text(text = "Animal: ${animal?.name ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                                vacc.nextDueDate?.let {
                                    val date = Date(it)
                                    val isOverdue = it < System.currentTimeMillis()
                                    Text(
                                        text = "Date: ${dateFormat.format(date)}",
                                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
