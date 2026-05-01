package com.example.ourgramavaxi.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AddAnimalScreen(navController: NavHostController, viewModel: AnimalViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf(AnimalConstants.SPECIES[0]) } // Default to Sheep
    var selectedBreed by remember { mutableStateOf("") }
    var selectedBreedResId by remember { mutableStateOf<Int?>(null) }
    var otherBreed by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(AnimalConstants.GENDERS[0]) }
    var selectedDistrict by remember { mutableStateOf(AnimalConstants.DISTRICTS[0].first) }
    var selectedDistrictResId by remember { mutableStateOf(AnimalConstants.DISTRICTS[0].second) }
    var age by remember { mutableStateOf("") }
    
    // Map to store last vaccination dates for each vaccine
    val vaccineDates = remember { mutableStateMapOf<String, Long?>() }
    
    val breeds = if (selectedSpecies == "Sheep") AnimalConstants.SHEEP_BREEDS else AnimalConstants.GOAT_BREEDS
    var breedExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

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
                                selectedBreed = "" // Reset breed on species change
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

            // Gender
            item {
                Text(stringResource(R.string.gender), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimalConstants.GENDERS.forEach { gender ->
                        RadioButton(
                            selected = (selectedGender == gender),
                            onClick = { selectedGender = gender }
                        )
                        Text(
                            text = if (gender == "Male") stringResource(R.string.male) else stringResource(R.string.female),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            // Age
            item {
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                    label = { Text(stringResource(R.string.age_years)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }

            // Vaccination History Header
            item {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.last_vaccination),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // List of Vaccines
            val vaccines = listOf(
                VaccineConstants.FMD to R.string.fmd_vaccine,
                VaccineConstants.PPR to R.string.ppr_vaccine,
                VaccineConstants.POX to R.string.pox_vaccine,
                VaccineConstants.HS to R.string.hs_vaccine,
                VaccineConstants.BLUETONGUE to R.string.bluetongue_vaccine,
                VaccineConstants.ENTEROTOXEMIA to R.string.enterotoxemia,
                VaccineConstants.ANTHRAX to R.string.anthrax_vaccine
            )

            items(vaccines) { (vaccineKey, vaccineResId) ->
                VaccineDatePickerField(
                    vaccineName = stringResource(vaccineResId),
                    selectedDate = vaccineDates[vaccineKey],
                    onDateSelected = { vaccineDates[vaccineKey] = it }
                )
            }

            // Save Button
            item {
                val isValid = name.isNotBlank() && age.isNotBlank() && (selectedBreed.isNotBlank() || otherBreed.isNotBlank())
                Button(
                    onClick = {
                        if (isValid) {
                            val finalBreed = if (selectedBreed == "Others") otherBreed else selectedBreed
                            viewModel.addAnimal(
                                name = name,
                                species = selectedSpecies,
                                breed = finalBreed,
                                gender = selectedGender,
                                ageInYears = age.toIntOrNull() ?: 0,
                                district = selectedDistrict,
                                lastVaccineDates = vaccineDates.toMap()
                            )
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
fun VaccineDatePickerField(
    vaccineName: String,
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
                }.timeInMillis
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Wrap in Box to capture clicks since OutlinedTextField with readOnly=true 
    // doesn't always trigger clicks reliably across all versions.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDatePicker() }
    ) {
        OutlinedTextField(
            value = if (selectedDate != null) sdf.format(Date(selectedDate)) else "",
            onValueChange = {},
            readOnly = true,
            enabled = false, // Set to false so the Box click listener works reliably
            label = { Text(vaccineName) },
            trailingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.pick_date))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }
}
