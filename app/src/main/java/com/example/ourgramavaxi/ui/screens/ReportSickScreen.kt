package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.Animal
import com.example.ourgramavaxi.data.AnimalConstants
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSickScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    var symptoms by remember { mutableStateOf("") }
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }
    var selectedDisease by remember { mutableStateOf("") }
    var otherDisease by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Medium") }
    
    var animalExpanded by remember { mutableStateOf(false) }
    var diseaseExpanded by remember { mutableStateOf(false) }
    
    val animals by viewModel.allAnimals.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_sick)) },
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
            // Animal Selection
            item {
                Text("Select Animal (ಪ್ರಾಣಿಯನ್ನು ಆರಿಸಿ)", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = animalExpanded,
                    onExpandedChange = { animalExpanded = !animalExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAnimal?.name ?: "Select",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = animalExpanded,
                        onDismissRequest = { animalExpanded = false }
                    ) {
                        animals.forEach { animal ->
                            DropdownMenuItem(
                                text = { Text("${animal.name} (${animal.breed})") },
                                onClick = {
                                    selectedAnimal = animal
                                    animalExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Disease Selection
            item {
                Text("Known Disease? (ರೋಗದ ಬಗ್ಗೆ ಗೊತ್ತಿದೆಯೇ?)", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = diseaseExpanded,
                    onExpandedChange = { diseaseExpanded = !diseaseExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDisease.ifEmpty { "Select if known" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diseaseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = diseaseExpanded,
                        onDismissRequest = { diseaseExpanded = false }
                    ) {
                        AnimalConstants.DISEASES.forEach { disease ->
                            DropdownMenuItem(
                                text = { Text(disease) },
                                onClick = {
                                    selectedDisease = disease
                                    diseaseExpanded = false
                                }
                            )
                        }
                    }
                }
                
                if (selectedDisease == "Others") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otherDisease,
                        onValueChange = { otherDisease = it },
                        label = { Text("Enter Disease Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Severity
            item {
                Text("Severity (ತೀವ್ರತೆ)", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { level ->
                        FilterChip(
                            selected = severity == level,
                            onClick = { severity = level },
                            label = { Text(level) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Symptoms Description
            item {
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms Description (ರೋಗದ ಲಕ್ಷಣಗಳು)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("e.g. Fever, not eating...") }
                )
            }

            // Submit Button
            item {
                val canSubmit = selectedAnimal != null && symptoms.isNotBlank()
                Button(
                    onClick = {
                        if (canSubmit) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Alert sent to Veterinary Assistant! (ವೆಟ್ ವೈದ್ಯರಿಗೆ ತಿಳಿಸಲಾಗಿದೆ)")
                                navController.popBackStack()
                            }
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (severity == "High") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Send Emergency Alert (ತುರ್ತು ಎಚ್ಚರಿಕೆ ಕಳುಹಿಸಿ)")
                }
            }
        }
    }
}
