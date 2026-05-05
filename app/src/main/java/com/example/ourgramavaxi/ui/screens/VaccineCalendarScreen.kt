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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    val activeCampaigns = VaccineConstants.SEASONAL_WINDOWS
        .filter { it.value.contains(currentMonth) }.keys.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vaccine_calendar)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.active_campaign_window),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.govt_prioritizing, activeCampaigns.joinToString(", ")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.animal_schedule),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // BUG 9 FIX: Removed redundant `.filter { !it.isCompleted }` —
            // getAllUpcomingVaccinations() already filters isCompleted = 0 at the SQL level.
            // Keeping the filter was wasteful and confusing.
            val upcoming = vaccinations.sortedBy { it.nextDueDate }

            if (upcoming.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_vaccinations_scheduled),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(upcoming) { vacc ->
                    val animal = animals.find { it.id == vacc.animalId }

                    // BUG 6 FIX: Map vaccine name to localized string resource.
                    // Old code showed raw English key ("FMD Vaccine") even in Kannada mode.
                    val vaccineResId = when (vacc.vaccineName) {
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
                    val vaccineDisplayName = if (vaccineResId != null)
                        stringResource(vaccineResId)
                    else
                        vacc.vaccineName

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.small
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = vaccineDisplayName, fontWeight = FontWeight.Bold)
                                Text(
                                    text = stringResource(R.string.animal_label, animal?.name ?: "Unknown"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                vacc.nextDueDate?.let {
                                    val isOverdue = it < System.currentTimeMillis()
                                    Text(
                                        text = stringResource(R.string.date_label, dateFormat.format(Date(it))),
                                        color = if (isOverdue) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary,
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