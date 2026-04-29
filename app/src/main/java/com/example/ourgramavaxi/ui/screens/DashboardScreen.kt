package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Grama-Vaxi (ಗ್ರಾಮ-ವ್ಯಾಕ್ಸಿ)") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome, Farmer!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard("Animal Ledger\n(ಪ್ರಾಣಿಗಳ ಪಟ್ಟಿ)", Icons.Default.List, Modifier.weight(1f)) {
                    navController.navigate("ledger")
                }
                DashboardCard("Vaccine Calendar\n(ಲಸಿಕೆ ಕ್ಯಾಲೆಂಡರ್)", Icons.Default.DateRange, Modifier.weight(1f)) {}
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard("Camp Alerts\n(ಕ್ಯಾಂಪ್ ಅಲರ್ಟ್)", Icons.Default.Notifications, Modifier.weight(1f)) {}
                DashboardCard("Report Sick\n(ಅನಾರೋಗ್ಯ ವರದಿ)", Icons.Default.Warning, Modifier.weight(1f)) {}
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}
