package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.viewmodel.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Pets, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.app_name))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (currentLang == "en") "ಕನ್ನಡ" else "English",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { navController.navigate("vaccine_calendar") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
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
            Text(
                text = stringResource(R.string.dashboard),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    stringResource(R.string.animal_ledger), 
                    Icons.Default.Pets, 
                    MaterialTheme.colorScheme.primaryContainer,
                    Modifier.weight(1f)
                ) {
                    navController.navigate("animal_ledger")
                }
                DashboardCard(
                    stringResource(R.string.vaccine_calendar), 
                    Icons.Default.Event, 
                    MaterialTheme.colorScheme.secondaryContainer,
                    Modifier.weight(1f)
                ) {
                    navController.navigate("vaccine_calendar")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    stringResource(R.string.camp_alerts), 
                    Icons.Default.Campaign, 
                    MaterialTheme.colorScheme.tertiaryContainer,
                    Modifier.weight(1f)
                ) {
                    navController.navigate("camp_alerts")
                }
                DashboardCard(
                    stringResource(R.string.report_sick), 
                    Icons.Default.HealthAndSafety, 
                    MaterialTheme.colorScheme.errorContainer,
                    Modifier.weight(1f)
                ) {
                    navController.navigate("report_sick")
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
        }
    }
}
