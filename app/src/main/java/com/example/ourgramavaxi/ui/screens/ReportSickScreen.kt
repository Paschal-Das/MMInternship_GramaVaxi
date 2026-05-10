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

    // BUG 5 FIX: Read all display strings ONCE here so they're available inside callbacks
    val alertSentMsg = stringResource(R.string.alert_sent_message)
    val lowLabel = stringResource(R.string.low)
    val mediumLabel = stringResource(R.string.medium)
    val highLabel = stringResource(R.string.high)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_sick)) },
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
            // Animal Selection
            item {
                // BUG 5 FIX: Use stringResource instead of hardcoded "Select Animal (ಪ್ರಾಣಿಯನ್ನು ಆರಿಸಿ)"
                Text(stringResource(R.string.select_animal_label), fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = animalExpanded,
                    onExpandedChange = { animalExpanded = !animalExpanded }
                ) {
                    OutlinedTextField(
                        // BUG 5 FIX: Use stringResource instead of hardcoded "Select"
                        value = selectedAnimal?.name ?: stringResource(R.string.select_label),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = animalExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = diseaseExpanded,
                        onDismissRequest = { diseaseExpanded = false }
                    ) {
                        // ✅ BUG 5 FIX: Iterate over (key, resId) pairs and display localized name
                        AnimalConstants.DISEASES.forEach { (diseaseKey, diseaseResId) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(diseaseResId)) },
                                onClick = {
                                    selectedDisease = diseaseKey
                                    diseaseExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Disease Selection
            item {
                // BUG 5 FIX: Use stringResource instead of hardcoded "Known Disease? ..."
                Text(stringResource(R.string.known_disease_label), fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = diseaseExpanded,
                    onExpandedChange = { diseaseExpanded = !diseaseExpanded }
                ) {
                    OutlinedTextField(
                        // BUG 5 FIX: Use stringResource
                        // ✅ BUG 5 FIX: Show localized disease name when one is selected
                        value = if (selectedDisease.isEmpty()) {
                            stringResource(R.string.select_if_known)
                        } else {
                            val resId = AnimalConstants.DISEASES.find { it.first == selectedDisease }?.second
                            if (resId != null) stringResource(resId) else selectedDisease
                        },
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
                        // BUG 5 FIX: Use stringResource
                        label = { Text(stringResource(R.string.enter_disease_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Severity
            item {
                // BUG 5 FIX: Use stringResource
                Text(stringResource(R.string.severity_label), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // BUG 5 FIX: Use string resources for severity labels
                    listOf("Low" to lowLabel, "Medium" to mediumLabel, "High" to highLabel)
                        .forEach { (key, label) ->
                            FilterChip(
                                selected = severity == key,
                                onClick = { severity = key },
                                label = { Text(label) },
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
                    // BUG 5 FIX: Use stringResource
                    label = { Text(stringResource(R.string.symptoms_description_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text(stringResource(R.string.symptoms_placeholder)) }
                )
            }

            // Submit Button
            item {
                val canSubmit = selectedAnimal != null && symptoms.isNotBlank()
                Button(
                    onClick = {
                        if (canSubmit) {
                            scope.launch {
                                // BUG 5 FIX: Use stringResource (pre-loaded above)
                                snackbarHostState.showSnackbar(alertSentMsg)
                                navController.popBackStack()
                            }
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (severity == "High")
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    // BUG 5 FIX: Use stringResource
                    Text(stringResource(R.string.send_emergency_alert))
                }
            }
        }
    }
}