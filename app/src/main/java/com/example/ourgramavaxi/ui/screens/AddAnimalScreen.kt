package com.example.ourgramavaxi.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.AnimalConstants
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(navController: NavHostController, viewModel: AnimalViewModel, animalId: Int? = null) {
    val context = LocalContext.current
    val animals by viewModel.allAnimals.collectAsState(initial = emptyList())
    val existingAnimal = remember(animalId, animals) { animals.find { it.id == animalId } }

    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf(AnimalConstants.SPECIES[0]) } 
    var selectedBreed by remember { mutableStateOf("") }
    var selectedBreedResId by remember { mutableStateOf<Int?>(null) }
    var otherBreed by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(AnimalConstants.GENDERS[0]) }
    var selectedDistrict by remember { mutableStateOf(AnimalConstants.DISTRICTS[0].first) }
    var selectedDistrictResId by remember { mutableStateOf(AnimalConstants.DISTRICTS[0].second) }
    var age by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Simplified Vaccination State
    var selectedVaccine by remember { mutableStateOf<String?>(null) }
    var selectedVaccineResId by remember { mutableStateOf<Int?>(null) }
    var customVaccineName by remember { mutableStateOf("") }
    var lastVaccineDate by remember { mutableStateOf<Long?>(null) }
    var nextVaccineDate by remember { mutableStateOf<Long?>(null) }

    // Initialize with existing data if editing
    LaunchedEffect(existingAnimal) {
        existingAnimal?.let {
            name = it.name
            selectedSpecies = it.species
            selectedBreed = it.breed
            selectedGender = it.gender
            selectedDistrict = it.district
            selectedDistrictResId = AnimalConstants.DISTRICTS.find { d -> d.first == it.district }?.second ?: AnimalConstants.DISTRICTS[0].second
            age = it.ageInYears.toString()
            notes = it.notes
            
            // Note: Vaccinations are complex to prepopulate in a simple dropdown, 
            // but the main data fields are now editable.
        }
    }
    
    val breeds = if (selectedSpecies == "Sheep") AnimalConstants.SHEEP_BREEDS else AnimalConstants.GOAT_BREEDS
    var breedExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var vaccineExpanded by remember { mutableStateOf(false) }

    val vaccines = listOf(
        VaccineConstants.FMD to R.string.fmd_vaccine,
        VaccineConstants.PPR to R.string.ppr_vaccine,
        VaccineConstants.POX to R.string.pox_vaccine,
        VaccineConstants.HS to R.string.hs_vaccine,
        VaccineConstants.BLUETONGUE to R.string.bluetongue_vaccine,
        VaccineConstants.ENTEROTOXEMIA to R.string.enterotoxemia,
        VaccineConstants.ANTHRAX to R.string.anthrax_vaccine,
        "Others" to R.string.others
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_animal)) },
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
            // Name/Tag
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.animal_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Species Selection
            item {
                Text(stringResource(R.string.species), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimalConstants.SPECIES.forEach { species ->
                        RadioButton(
                            selected = (selectedSpecies == species),
                            onClick = { 
                                selectedSpecies = species
                                selectedBreed = "" 
                            }
                        )
                        Text(
                            text = if (species == "Sheep") stringResource(R.string.sheep) else stringResource(R.string.goat),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            // Breed Selection
            item {
                ExposedDropdownMenuBox(
                    expanded = breedExpanded,
                    onExpandedChange = { breedExpanded = !breedExpanded }
                ) {
                    val displayText = if (selectedBreed == "Others") {
                        "${stringResource(R.string.others)}: $otherBreed"
                    } else if (selectedBreedResId != null) {
                        stringResource(selectedBreedResId!!)
                    } else {
                        selectedBreed
                    }
                    OutlinedTextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_breed)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = breedExpanded,
                        onDismissRequest = { breedExpanded = false }
                    ) {
                        breeds.forEach { (b, resId) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    selectedBreed = b
                                    selectedBreedResId = resId
                                    breedExpanded = false
                                }
                            )
                        }
                    }
                }
                
                if (selectedBreed == "Others") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otherBreed,
                        onValueChange = { otherBreed = it },
                        label = { Text(stringResource(R.string.enter_breed_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Age
            item {
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                    label = { Text(stringResource(R.string.age_years)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // District Selection
            item {
                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = !districtExpanded }
                ) {
                    OutlinedTextField(
                        value = stringResource(selectedDistrictResId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_district)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        AnimalConstants.DISTRICTS.forEach { (d, resId) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    selectedDistrict = d
                                    selectedDistrictResId = resId
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Notes / Description
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // Simplified Vaccination Section
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.vaccination_history),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = vaccineExpanded,
                    onExpandedChange = { vaccineExpanded = !vaccineExpanded }
                ) {
                    val displayText = if (selectedVaccine == "Others") {
                        "${stringResource(R.string.others)}: $customVaccineName"
                    } else if (selectedVaccineResId != null) {
                        stringResource(selectedVaccineResId!!)
                    } else {
                        ""
                    }
                    
                    OutlinedTextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Vaccine (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vaccineExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = vaccineExpanded,
                        onDismissRequest = { vaccineExpanded = false }
                    ) {
                        vaccines.forEach { (vKey, vResId) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(vResId)) },
                                onClick = {
                                    selectedVaccine = vKey
                                    selectedVaccineResId = vResId
                                    vaccineExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedVaccine == "Others") {
                item {
                    OutlinedTextField(
                        value = customVaccineName,
                        onValueChange = { customVaccineName = it },
                        label = { Text("Enter Vaccine Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (selectedVaccine != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatePickerField(
                            label = "Last Vaccination Date",
                            selectedDate = lastVaccineDate,
                            onDateSelected = { lastVaccineDate = it }
                        )
                        DatePickerField(
                            label = "Next Vaccination Date",
                            selectedDate = nextVaccineDate,
                            onDateSelected = { nextVaccineDate = it }
                        )
                    }
                }
            }

            // Save Button
            item {
                val isValid = name.isNotBlank() && age.isNotBlank() && (selectedBreed.isNotBlank() || otherBreed.isNotBlank())
                Button(
                    onClick = {
                        if (isValid) {
                            val finalBreed = if (selectedBreed == "Others") otherBreed else selectedBreed
                            val finalVaccineName = if (selectedVaccine == "Others") customVaccineName else selectedVaccine
                            
                            val vaccineMap = mutableMapOf<String, Long?>()
                            val nextVaccineMap = mutableMapOf<String, Long?>()
                            if (finalVaccineName != null) {
                                vaccineMap[finalVaccineName] = lastVaccineDate
                                nextVaccineMap[finalVaccineName] = nextVaccineDate
                            }

                            if (animalId != null) {
                                viewModel.updateAnimal(
                                    id = animalId,
                                    name = name,
                                    species = selectedSpecies,
                                    breed = finalBreed,
                                    gender = selectedGender,
                                    ageInYears = age.toIntOrNull() ?: 0,
                                    district = selectedDistrict,
                                    notes = notes,
                                    photoUri = null,
                                    lastVaccineDates = vaccineMap,
                                    nextVaccineDates = nextVaccineMap
                                )
                            } else {
                                viewModel.addAnimal(
                                    name = name,
                                    species = selectedSpecies,
                                    breed = finalBreed,
                                    gender = selectedGender,
                                    ageInYears = age.toIntOrNull() ?: 0,
                                    district = selectedDistrict,
                                    notes = notes,
                                    photoUri = null, 
                                    lastVaccineDates = vaccineMap,
                                    nextVaccineDates = nextVaccineMap
                                )
                            }
                            navController.popBackStack()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val showDatePicker = {
        val calendar = Calendar.getInstance()
        if (selectedDate != null) {
            calendar.timeInMillis = selectedDate
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (selectedDate != null) sdf.format(Date(selectedDate)) else "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker() }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker() }
        )
    }
}
