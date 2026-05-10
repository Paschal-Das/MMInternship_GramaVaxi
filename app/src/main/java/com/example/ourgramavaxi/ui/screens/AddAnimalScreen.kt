package com.example.ourgramavaxi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ourgramavaxi.R
import com.example.ourgramavaxi.data.AnimalConstants
import com.example.ourgramavaxi.data.VaccineConstants
import com.example.ourgramavaxi.viewmodel.AnimalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(
    navController: NavHostController,
    viewModel: AnimalViewModel,
    animalId: Int? = null
) {
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

    // PHOTO FIX: State to hold the URI of the photo chosen by the user
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    var selectedVaccine by remember { mutableStateOf<String?>(null) }
    var selectedVaccineResId by remember { mutableStateOf<Int?>(null) }
    var customVaccineName by remember { mutableStateOf("") }
    var lastVaccineDate by remember { mutableStateOf<Long?>(null) }
    var nextVaccineDate by remember { mutableStateOf<Long?>(null) }

    // Load existing animal data when editing
    LaunchedEffect(existingAnimal) {
        existingAnimal?.let {
            name = it.name
            selectedSpecies = it.species
            selectedBreed = it.breed
            selectedGender = it.gender
            selectedDistrict = it.district
            selectedDistrictResId = AnimalConstants.DISTRICTS
                .find { d -> d.first == it.district }?.second
                ?: AnimalConstants.DISTRICTS[0].second
            age = it.ageInYears.toString()
            notes = it.notes
            // PHOTO FIX: Load existing photo URI when editing
            selectedPhotoUri = it.photoUri
        }
    }

    val breeds = if (selectedSpecies == "Sheep") AnimalConstants.SHEEP_BREEDS else AnimalConstants.GOAT_BREEDS
    var breedExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var vaccineExpanded by remember { mutableStateOf(false) }

    // BUG 8 FIX: Added CCPP vaccine which was missing from the dropdown
    val vaccines = listOf(
        VaccineConstants.FMD to R.string.fmd_vaccine,
        VaccineConstants.PPR to R.string.ppr_vaccine,
        VaccineConstants.POX to R.string.pox_vaccine,
        VaccineConstants.HS to R.string.hs_vaccine,
        VaccineConstants.BLUETONGUE to R.string.bluetongue_vaccine,
        VaccineConstants.ENTEROTOXEMIA to R.string.enterotoxemia,
        VaccineConstants.CCPP to R.string.ccpp_vaccine,       // ← ADDED (was missing)
        VaccineConstants.ANTHRAX to R.string.anthrax_vaccine,
        "Others" to R.string.others
    )

    // ─── PHOTO FIX: Image picker launcher ────────────────────────────────────────────
    // This opens the phone's gallery. No file provider or complex setup needed.
    // ✅ BUG A FIX: Copy the photo into the app's private storage immediately.
// content:// URIs are temporary — they expire when the app closes.
// Copying to filesDir gives the app permanent ownership of the image.
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val savedPath = copyPhotoToInternalStorage(context, it)
            selectedPhotoUri = savedPath ?: it.toString()
        }
    }
    /**
     * Copies a photo from any URI source (gallery, files app, etc.) into
     * the app's private internal storage. This permanently preserves the photo
     * even after the original content:// URI permission expires on app restart.
     *
     * Returns a "file://" URI string that Coil's AsyncImage can load directly,
     * or null if the copy fails (e.g. user denied storage permission).
     */
    fun copyPhotoToInternalStorage(context: android.content.Context, sourceUri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            // Unique filename based on timestamp to prevent collisions
            val fileName = "animal_photo_${System.currentTimeMillis()}.jpg"
            val outputFile = java.io.File(context.filesDir, fileName)
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            // Return as file:// URI — Coil's AsyncImage handles this format everywhere
            "file://${outputFile.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            null // Photo won't be saved, but app won't crash
        }
    }

    // Permission launcher — needed for Android 13+ (READ_MEDIA_IMAGES)
    // and Android 6–12 (READ_EXTERNAL_STORAGE)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePicker.launch("image/*")
        }
        // If denied, the photo area stays blank — app doesn't crash
    }

    // Helper: decide which permission to request based on Android version
    fun openPhotoPicker() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: need READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                    imagePicker.launch("image/*")
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6–12: need READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    imagePicker.launch("image/*")
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> {
                // Android 5 and below: no runtime permission needed
                imagePicker.launch("image/*")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (animalId != null) stringResource(R.string.edit_animal)
                        else stringResource(R.string.add_animal)
                    )
                },
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

            // ─── PHOTO FIX: Photo Picker ──────────────────────────────────────────────
            item {
                Text(
                    text = stringResource(R.string.animal_photo),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { openPhotoPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPhotoUri != null) {
                        // Show selected photo
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = stringResource(R.string.animal_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Dark overlay so the "change" label is readable
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = stringResource(R.string.change_photo),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    } else {
                        // Placeholder when no photo is selected
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.add_photo),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- Animal Name ---
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.animal_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- Species ---
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
                            text = if (species == "Sheep") stringResource(R.string.sheep)
                            else stringResource(R.string.goat),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            // --- Gender ---
            item {
                Text(stringResource(R.string.gender), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimalConstants.GENDERS.forEach { g ->
                        RadioButton(
                            selected = (selectedGender == g),
                            onClick = { selectedGender = g }
                        )
                        Text(
                            text = if (g == "Male") stringResource(R.string.male)
                            else stringResource(R.string.female),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            // --- Breed ---
            item {
                ExposedDropdownMenuBox(
                    expanded = breedExpanded,
                    onExpandedChange = { breedExpanded = !breedExpanded }
                ) {
                    val displayText = when {
                        selectedBreed == "Others" -> "${stringResource(R.string.others)}: $otherBreed"
                        selectedBreedResId != null -> stringResource(selectedBreedResId!!)
                        else -> selectedBreed
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

            // --- Age ---
            item {
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                    label = { Text(stringResource(R.string.age_years)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // --- District ---
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

            // --- Notes ---
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // --- Vaccination Section Header ---
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

            // --- Vaccine Picker ---
            item {
                ExposedDropdownMenuBox(
                    expanded = vaccineExpanded,
                    onExpandedChange = { vaccineExpanded = !vaccineExpanded }
                ) {
                    val displayText = when {
                        selectedVaccine == "Others" -> "${stringResource(R.string.others)}: $customVaccineName"
                        selectedVaccineResId != null -> stringResource(selectedVaccineResId!!)
                        else -> ""
                    }
                    OutlinedTextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_vaccine_optional)) },
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
                        label = { Text(stringResource(R.string.enter_vaccine_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (selectedVaccine != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ComposeDatePickerField(
                            label = stringResource(R.string.last_vaccination_date),
                            selectedDate = lastVaccineDate,
                            onDateSelected = { lastVaccineDate = it }
                        )
                        ComposeDatePickerField(
                            label = stringResource(R.string.next_vaccination_date),
                            selectedDate = nextVaccineDate,
                            onDateSelected = { nextVaccineDate = it }
                        )
                    }
                }
            }

            // --- Save Button ---
            item {
                val finalBreed = if (selectedBreed == "Others") otherBreed else selectedBreed
                val isValid = name.isNotBlank() && age.isNotBlank() && finalBreed.isNotBlank()

                Button(
                    onClick = {
                        if (isValid) {
                            val finalVaccineName =
                                if (selectedVaccine == "Others") customVaccineName else selectedVaccine
                            val vaccineMap = mutableMapOf<String, Long?>()
                            val nextVaccineMap = mutableMapOf<String, Long?>()
                            if (finalVaccineName != null) {
                                vaccineMap[finalVaccineName] = lastVaccineDate
                                nextVaccineMap[finalVaccineName] = nextVaccineDate
                            }

                            if (animalId != null) {
                                viewModel.updateAnimal(
                                    id = animalId, name = name, species = selectedSpecies,
                                    breed = finalBreed, gender = selectedGender,
                                    ageInYears = age.toIntOrNull() ?: 0,
                                    district = selectedDistrict, notes = notes,
                                    // PHOTO FIX: Pass the actual selected photo URI
                                    photoUri = selectedPhotoUri,
                                    lastVaccineDates = vaccineMap, nextVaccineDates = nextVaccineMap
                                )
                            } else {
                                viewModel.addAnimal(
                                    name = name, species = selectedSpecies,
                                    breed = finalBreed, gender = selectedGender,
                                    ageInYears = age.toIntOrNull() ?: 0,
                                    district = selectedDistrict, notes = notes,
                                    // PHOTO FIX: Pass the actual selected photo URI
                                    photoUri = selectedPhotoUri,
                                    lastVaccineDates = vaccineMap, nextVaccineDates = nextVaccineMap
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeDatePickerField(
    label: String,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
    )

    OutlinedTextField(
        value = if (selectedDate != null) sdf.format(Date(selectedDate)) else "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}