# Grama-Vaxi — Complete Project Documentation

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Setup and Run Instructions](#2-setup-and-run-instructions)
3. [Project Architecture](#3-project-architecture)
4. [Folder Structure](#4-folder-structure)
5. [Data Layer](#5-data-layer)
6. [Domain Layer](#6-domain-layer)
7. [Repository Layer](#7-repository-layer)
8. [ViewModel](#8-viewmodel)
9. [UI Screens](#9-ui-screens)
10. [Background Workers](#10-background-workers)
11. [Notifications](#11-notifications)
12. [Language Support](#12-language-support)
13. [Dependency Injection](#13-dependency-injection)
14. [Dependencies and Libraries](#14-dependencies-and-libraries)
15. [Known Limitations](#15-known-limitations)

---

## 1. Project Overview

**App Name:** Grama-Vaxi
**Package:** `com.example.ourgramavaxi`
**Version:** 1.0
**Min SDK:** API 24 (Android 7.0 Nougat)
**Target SDK:** API 35 (Android 15)
**Language:** Kotlin
**UI Framework:** Jetpack Compose

Grama-Vaxi is an offline-first Android app built for rural farmers in Karnataka. It gives each sheep or goat a digital health card, automatically calculates the next vaccination due date based on government-standard intervals, and sends push notifications before vaccination camps reach the village — even if the app has not been opened for days.

The app works completely without an internet connection. All data is stored locally on the device using Room (SQLite). WorkManager handles background notification scheduling so reminders fire reliably regardless of whether the app is running.

---

## 2. Setup and Run Instructions

### Prerequisites

Before opening the project, make sure you have the following installed on your computer:

- **Android Studio** — Hedgehog (2023.1.1) or any later version
- **JDK 11** — Android Studio installs this automatically
- **Android SDK** — Compile SDK 35, Build Tools 35.0.0 (installed via Android Studio's SDK Manager)
- **Kotlin** — Version 1.9 or later (bundled with Android Studio)
- A physical Android device running Android 7.0 or higher, OR an Android Emulator configured with API 24 or above

### Step 1 — Clone the Repository

Open a terminal (Command Prompt, PowerShell, or Terminal on Mac/Linux) and run:

```
git clone https://github.com/Paschal-Das/MMInternship_GramaVaxi.git
```

Or download the ZIP from GitHub and extract it to a folder on your computer.(preferred)

### Step 2 — Open in Android Studio

1. Launch Android Studio
2. Click **File → Open**
3. Navigate to the `OurGramaVaxi` folder (the root — the folder that contains `app/`, `gradle/`, and `build.gradle.kts`)
4. Click **OK**

Do not open the `app/` subfolder — always open the root project folder.

### Step 3 — Wait for Gradle Sync

Android Studio will automatically start downloading all libraries and syncing the project. This takes 2 to 5 minutes on the first run depending on your internet speed. You will see a progress bar at the bottom of the screen. Wait until it says **"Gradle sync finished"** before doing anything else.

If Gradle sync fails, try:
- **File → Invalidate Caches → Invalidate and Restart**
- Check your internet connection — Gradle needs to download libraries on first run
- Make sure your Android SDK is installed via **File → Settings → Appearance & Behavior → System Settings → Android SDK**

### Step 4 — Run the App

**On a physical device:**
1. On your Android phone, go to **Settings → About Phone** and tap **Build Number** seven times to enable Developer Options
2. Go to **Settings → Developer Options** and turn on **USB Debugging**
3. Connect your phone to your computer with a USB cable
4. In Android Studio, select your device from the device dropdown at the top toolbar
5. Click the green **Run** button (▶) or press **Shift + F10**

**On an emulator:**
1. Go to **Tools → Device Manager** in Android Studio
2. Click **Create Device**, choose a phone (e.g. Pixel 6), and select API 35 as the system image
3. Click **Finish** to create the emulator
4. Select the emulator from the device dropdown and click **Run**

### Step 5 — First Launch

On first launch, the app automatically seeds two demo animals — Muttu (sheep) and Gauri (goat) — with vaccination records so you can see all features immediately without needing to register animals manually.

### No API Keys or Internet Required

This app does not use any external API or cloud service. No API keys, no environment files, and no internet connection are required to build or run it.

---

## 3. Project Architecture

Grama-Vaxi uses a clean layered architecture with clear separation between UI, business logic, and data storage.

```
┌─────────────────────────────────────┐
│             UI Layer                │
│    Jetpack Compose Screens          │
│    (screens/, theme/)               │
└───────────────┬─────────────────────┘
                │ observes StateFlow / Flow
┌───────────────▼─────────────────────┐
│          ViewModel Layer            │
│    AnimalViewModel                  │
│    (single ViewModel for all screens│
└───────────────┬─────────────────────┘
                │ calls
┌───────────────▼─────────────────────┐
│          Domain Layer               │
│    RegisterAnimalUseCase            │
│    UpdateAnimalUseCase              │
└───────────────┬─────────────────────┘
                │ calls
┌───────────────▼─────────────────────┐
│         Repository Layer            │
│    AnimalRepository (interface)     │
│    AnimalRepositoryImpl             │
│    PreferenceRepository             │
│    PreferenceRepositoryImpl         │
└───────────────┬─────────────────────┘
                │ reads / writes
┌───────────────▼─────────────────────┐
│           Data Layer                │
│    Room Database (AppDatabase)      │
│    AnimalDao, VaccinationDao,       │
│    CampAlertDao                     │
│    SharedPreferences                │
└─────────────────────────────────────┘

Background:
┌─────────────────────────────────────┐
│         WorkManager                 │
│    VaccineWorker (every 24 hours)   │
│    CampReminderWorker (every 24 hrs)│
│         → NotificationHelper        │
└─────────────────────────────────────┘
```

**Key architectural decisions:**

**Single ViewModel** — All screens share one `AnimalViewModel`. This avoids duplicating state and keeps the app simple. Since the data requirements are modest for a village-level app, a single ViewModel is appropriate.

**Use Cases for registration logic** — The logic for creating and updating animal vaccination records is complex enough to live in dedicated `UseCase` classes (`RegisterAnimalUseCase`, `UpdateAnimalUseCase`) rather than inside the ViewModel. This keeps the ViewModel thin.

**Offline-first** — Everything is stored in Room. The app is designed to work with zero connectivity. WorkManager ensures background tasks survive reboots and battery optimization.

**Interface-based repositories** — `AnimalRepository` and `PreferenceRepository` are interfaces, with concrete implementations injected by Hilt. This makes the code easier to test and change independently.

---

## 4. Folder Structure

```
app/src/main/
│
├── java/com/example/ourgramavaxi/
│   │
│   ├── GramaVaxiApplication.kt       ← App entry point, Hilt + WorkManager setup
│   ├── MainActivity.kt               ← Single Activity, Navigation host, locale setup
│   │
│   ├── data/                         ← Room entities, DAOs, database, constants
│   │   ├── Animal.kt                 ← Animal entity + AnimalConstants + VaccineConstants
│   │   ├── AnimalDao.kt              ← Animal database queries
│   │   ├── Vaccination.kt            ← Vaccination entity
│   │   ├── VaccinationDao.kt         ← Vaccination database queries
│   │   ├── CampAlert.kt              ← Camp alert entity
│   │   ├── CampAlertDao.kt           ← Camp alert database queries
│   │   └── AppDatabase.kt            ← Room database definition (version 7)
│   │
│   ├── di/                           ← Hilt dependency injection modules
│   │   ├── DatabaseModule.kt         ← Provides Room database, all DAOs
│   │   └── RepositoryModule.kt       ← Provides AnimalRepository, PreferenceRepository
│   │
│   ├── domain/usecase/               ← Business logic for animal registration
│   │   ├── RegisterAnimalUseCase.kt  ← Creates animal + vaccination records
│   │   └── UpdateAnimalUseCase.kt    ← Updates animal + vaccination records
│   │
│   ├── notifications/
│   │   └── NotificationHelper.kt     ← Creates notification channel, shows notifications
│   │
│   ├── repository/                   ← Data access abstraction
│   │   ├── AnimalRepository.kt       ← Interface for all animal/vaccination/camp operations
│   │   ├── AnimalRepositoryImpl.kt   ← Implementation delegating to DAOs
│   │   ├── PreferenceRepository.kt   ← Interface for language and alert preferences
│   │   └── PreferenceRepositoryImpl.kt ← SharedPreferences implementation
│   │
│   ├── ui/
│   │   ├── screens/                  ← All Compose UI screens
│   │   │   ├── DashboardScreen.kt    ← Home screen with navigation tiles
│   │   │   ├── AnimalLedgerScreen.kt ← List of all registered animals
│   │   │   ├── AddAnimalScreen.kt    ← Register or edit an animal (shared screen)
│   │   │   ├── AnimalDetailScreen.kt ← Full health record for one animal
│   │   │   ├── VaccineCalendarScreen.kt ← All upcoming vaccinations across all animals
│   │   │   ├── CampAlertsScreen.kt   ← Upcoming government vaccination camps
│   │   │   ├── ReportSickScreen.kt   ← Report a sick animal to simulated vet
│   │   │   └── GeneralInformationScreen.kt ← Disease guides, breed info
│   │   └── theme/
│   │       ├── Color.kt              ← App color palette
│   │       ├── Theme.kt              ← Material3 theme definition
│   │       └── Type.kt               ← Typography scale
│   │
│   ├── viewmodel/
│   │   └── AnimalViewModel.kt        ← Single ViewModel for all screens
│   │
│   └── worker/                       ← Background WorkManager workers
│       ├── VaccineWorker.kt          ← Checks upcoming vaccines, sends notifications
│       └── CampReminderWorker.kt     ← Checks upcoming camps, sends notifications
│
└── res/
    ├── values/strings.xml            ← English strings
    ├── values-kn/strings.xml         ← Kannada strings
    ├── values/colors.xml
    └── xml/
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```

---

## 5. Data Layer

### 5.1 Database

**File:** `data/AppDatabase.kt`

The app uses a single Room database named `grama_vaxi_database`. The current schema version is **7**. The database is configured with `fallbackToDestructiveMigration()`, which means if the schema changes, the database is wiped and rebuilt rather than migrated. This is acceptable for a student project but would need proper migration scripts in a production app.

The database contains three tables: `animals`, `vaccinations`, and `camp_alerts`.

### 5.2 Animal Entity

**File:** `data/Animal.kt`

The `Animal` table stores one row for each registered animal.

| Column | Type | Description |
|---|---|---|
| `id` | Int (auto) | Primary key, auto-generated |
| `name` | String | Farmer-given name (e.g. "Muttu") |
| `species` | String | "Sheep" or "Goat" |
| `breed` | String | Breed name (e.g. "Deccani", "Osmanabadi") |
| `gender` | String | "Male" or "Female" |
| `ageInYears` | Int | Age at registration |
| `district` | String | Karnataka district, defaults to "Mandya" |
| `notes` | String | Free-text notes, defaults to empty |
| `photoUri` | String? | File path to photo in internal storage, nullable |
| `dateAdded` | Long | Timestamp (milliseconds) when the animal was registered |

**AnimalConstants** (also in `Animal.kt`) — provides lists used to populate dropdowns in the form:
- `SPECIES` — Sheep, Goat
- `GENDERS` — Male, Female
- `DISTRICTS` — All 31 Karnataka districts, each paired with a string resource ID for Kannada localization
- `SHEEP_BREEDS` — Deccani, Bellary, Mandya, Bannur, Hassan, Others
- `GOAT_BREEDS` — Osmanabadi, Bidri, Malnad Gidda, Beetal, Kodagu, Others
- `DISEASES` — Used in the Report Sick form

**VaccineConstants** (also in `Animal.kt`) — the central configuration for all vaccine logic:

| Constant | Value | Purpose |
|---|---|---|
| `FMD` | "FMD Vaccine" | Foot and Mouth Disease vaccine name key |
| `PPR` | "PPR Vaccine" | Sheep and Goat Plague vaccine name key |
| `POX` | "Sheep/Goat Pox Vaccine" | Pox vaccine name key |
| `HS` | "HS Vaccine" | Haemorrhagic Septicaemia vaccine name key |
| `BLUETONGUE` | "Bluetongue Vaccine" | Bluetongue vaccine name key |
| `ENTEROTOXEMIA` | "Enterotoxemia" | Enterotoxemia vaccine name key |
| `CCPP` | "CCPP (Goat)" | Contagious Caprine Pleuropneumonia (goats only) |
| `ANTHRAX` | "Anthrax Vaccine" | Anthrax vaccine name key |
| `DUE_BADGE_DAYS` | 14 | Days before due date to show the "Due" badge in Animal Ledger |
| `NOTIFICATION_DAYS` | 3 | Days before due date to send a push notification |

**VACCINE_INTERVALS** — the number of days between doses for each vaccine, based on government guidelines:
- FMD: 180 days (every 6 months)
- Enterotoxemia: 180 days
- PPR: 1095 days (every 3 years)
- Pox, Bluetongue, CCPP, HS, Anthrax: 365 days (annual)

**SEASONAL_WINDOWS** — the months (0-indexed) when government campaigns typically run for each vaccine. Used by the Vaccine Calendar screen to show the "Active Campaign" banner.

**HOTSPOT_ZONES** — districts with historically high disease risk that trigger automatic vaccination reminders on animal registration. Currently only Anthrax is configured, covering Chamarajanagar, Mandya, and Mysuru.

### 5.3 Vaccination Entity

**File:** `data/Vaccination.kt`

The `vaccinations` table stores individual vaccination records. It has a foreign key relationship with the `animals` table — when an animal is deleted, all its vaccination records are automatically deleted (`CASCADE`).

| Column | Type | Description |
|---|---|---|
| `id` | Int (auto) | Primary key |
| `animalId` | Int | Foreign key linking to `animals.id` |
| `vaccineName` | String | Name of the vaccine (matches VaccineConstants keys) |
| `dateAdministered` | Long | Timestamp when the vaccine was given (0 if not yet given) |
| `nextDueDate` | Long? | Timestamp of the next due date, nullable |
| `isCompleted` | Boolean | true = historical record, false = upcoming reminder |

**Two-record design:** Every vaccine entry creates two separate rows. When a farmer says "I gave FMD on 1 Jan and next is due 1 July":
- Row 1: `dateAdministered = Jan 1`, `isCompleted = true` — appears in the animal's vaccination history tab
- Row 2: `dateAdministered = 0`, `nextDueDate = July 1`, `isCompleted = false` — appears in the Vaccine Calendar and is read by the notification worker

This separation is essential. The `getAllUpcomingVaccinations()` DAO query only reads rows where `isCompleted = 0`. If the next due date were stored only in the completed row, it would be permanently invisible to the calendar and all notifications.

### 5.4 CampAlert Entity

**File:** `data/CampAlert.kt`

The `camp_alerts` table stores government vaccination camp announcements.

| Column | Type | Description |
|---|---|---|
| `id` | Int (auto) | Primary key |
| `title` | String | Camp name (e.g. "FMD Vaccination Camp") |
| `description` | String | Details about the camp |
| `location` | String | Village location (e.g. "Grama Panchayat Office") |
| `date` | Long | Timestamp of the camp date |
| `type` | String | Type (e.g. "Vaccination Camp", "Health Drive") |

### 5.5 DAOs

**AnimalDao** (`data/AnimalDao.kt`)
- `getAllAnimals()` — returns all animals ordered by most recently added, as a reactive `Flow`
- `getAnimalById(id)` — returns a single animal by ID (suspend, one-shot)
- `getAnimalCount()` — returns count as a one-shot suspend (used for seed check)
- `getAnimalCountFlow()` — returns count as a reactive `Flow`
- `insertAnimal(animal)` — returns the new row's ID as `Long`
- `updateAnimal(animal)` — updates all fields by ID
- `deleteAnimal(animal)` — deletes by object reference
- `searchAnimals(query)` — LIKE search by name (currently not connected to a search UI)

**VaccinationDao** (`data/VaccinationDao.kt`)
- `getAllUpcomingVaccinations()` — `WHERE isCompleted = 0 AND nextDueDate IS NOT NULL ORDER BY nextDueDate ASC` — the core query feeding the Calendar and workers
- `getVaccinationsForAnimal(animalId)` — all records for one animal, ordered by most recent
- `getPendingVaccinationCount(animalId)` — count of pending `isCompleted = 0` records for one animal (used for the "Due" badge)
- `insertVaccination(vaccination)` — inserts a record
- `deleteSpecificVaccination(animalId, vaccineName)` — deletes all records for a specific vaccine name on a specific animal (used before re-inserting during edit)

**CampAlertDao** (`data/CampAlertDao.kt`)
- `getAllCampAlerts()` — all camps, ordered by date ascending
- `getUpcomingCamps(currentTime)` — only camps where `date >= currentTime`, ordered by date
- `insertCampAlert(campAlert)` — inserts a camp
- `deleteCampAlert(campAlert)` — deletes by object reference
- `deletePastCamps(cutoffTime)` — bulk delete camps older than a cutoff (utility, not currently called automatically)

---

## 6. Domain Layer

### RegisterAnimalUseCase

**File:** `domain/usecase/RegisterAnimalUseCase.kt`

Called when a new animal is saved from `AddAnimalScreen`. Accepts all animal profile fields plus two maps:
- `lastVaccineDates: Map<String, Long?>` — the vaccine name and the date it was last given
- `nextVaccineDates: Map<String, Long?>` — the vaccine name and the manually entered next due date

**Logic flow:**

1. Inserts the animal into the `animals` table and captures its new auto-generated ID
2. **Loop 1** — for each vaccine where the farmer provided a last-given date:
   - Calculates the next due date (uses the farmer's manually entered date if provided, otherwise adds the standard interval from `VaccineConstants.VACCINE_INTERVALS`)
   - Inserts a **history record** (`isCompleted = true`)
   - Inserts a separate **upcoming reminder record** (`isCompleted = false`) so the Calendar and workers can see it
3. **Loop 2** — for each vaccine where only a next due date was provided (no last-given date):
   - Checks `lastVaccineDates[vaccineName] == null` (not `containsKey`) to correctly detect null-valued keys
   - Inserts one upcoming reminder record
4. **Hotspot zone check** — if the animal's district is in `VaccineConstants.HOTSPOT_ZONES`, inserts an automatic vaccination reminder for the relevant vaccine
5. **Fallback** — if the farmer entered no vaccine information at all, inserts an "Initial Health Checkup" reminder due in 7 days

Returns the new animal's ID as `Long`.

### UpdateAnimalUseCase

**File:** `domain/usecase/UpdateAnimalUseCase.kt`

Called when editing an existing animal. Same logic as `RegisterAnimalUseCase` for the vaccination records, but before re-inserting any vaccine records, it first calls `deleteSpecificVaccination(animalId, vaccineName)` to remove the old records for that specific vaccine. This prevents duplicate rows from accumulating on repeated edits.

---

## 7. Repository Layer

### AnimalRepository

**File:** `repository/AnimalRepository.kt`

Interface defining all data operations. Concrete implementation is `AnimalRepositoryImpl`, which delegates directly to the three DAOs. The interface is injected by Hilt so it can be swapped for a test implementation in unit tests.

Operations are split into three groups:
- **Animals** — CRUD operations, count, search
- **Vaccinations** — insert, delete specific, get for animal, get all upcoming, get pending count
- **Camp Alerts** — insert, delete, get all, get upcoming

### PreferenceRepository

**File:** `repository/PreferenceRepository.kt`

Interface for two types of user preferences stored in `SharedPreferences` (file: `grama_vaxi_prefs`):

**Language preference (`lang` key)**
- Stores `"en"` or `"kn"`
- Exposed as a reactive `Flow<String>` via a `SharedPreferences.OnSharedPreferenceChangeListener`
- Written with `.apply()` (asynchronous background write)

**Registered alert IDs (`registered_alert_ids` key)**
- Stores a `Set<String>` of camp IDs the farmer has registered interest in
- `toggleAlertRegistration(alertId)` — adds the ID if not present, removes it if already present
- Exposed as a reactive `Flow<Set<Int>>`

---

## 8. ViewModel

**File:** `viewmodel/AnimalViewModel.kt`

A single `@HiltViewModel` shared across all screens. Injected into every screen via `hiltViewModel()` in `MainActivity`.

**Exposed state:**

| Property | Type | Source |
|---|---|---|
| `allAnimals` | `Flow<List<Animal>>` | `repository.getAllAnimals()` |
| `allUpcomingVaccinations` | `Flow<List<Vaccination>>` | `repository.getAllUpcomingVaccinations()` |
| `allCampAlerts` | `Flow<List<CampAlert>>` | `repository.getAllCampAlerts()` |
| `currentLanguage` | `StateFlow<String>` | `preferenceRepository.currentLanguage` |
| `registeredAlertIds` | `StateFlow<Set<Int>>` | `preferenceRepository.registeredAlertIds` |

**Key functions:**

- `addAnimal(...)` — calls `RegisterAnimalUseCase`, then `scheduleVaccineReminder()`
- `updateAnimal(...)` — calls `UpdateAnimalUseCase`, then `scheduleVaccineReminder()`
- `deleteAnimal(animal)` — deletes the animal (Room's CASCADE deletes all its vaccination records automatically)
- `getVaccinationsForAnimal(animalId)` — returns a `Flow` of vaccination records for one animal
- `toggleLanguage()` — reads current language, switches to the other, saves to `PreferenceRepository`
- `toggleAlertRegistration(alertId)` — toggles the farmer's registration interest for a camp
- `addCampAlert(...)` — inserts a new camp alert manually
- `scheduleVaccineReminder()` (private) — enqueues `VaccineWorker` as a unique periodic work running every 24 hours, with a 1-hour initial delay. Uses `KEEP` policy so re-registering does not reset the timer.
- `seedSampleData()` — called once on app launch from `MainActivity`. Checks `getAnimalCount()` first; if any animals exist, it does nothing. On a fresh install, inserts Muttu, Gauri, their vaccination records, and two camp alerts.

---

## 9. UI Screens

All screens are Jetpack Compose functions. Navigation between them is handled by `NavHost` in `MainActivity` using Compose Navigation.

**Navigation routes:**
- `dashboard` → `DashboardScreen`
- `animal_ledger` → `AnimalLedgerScreen`
- `add_animal` → `AddAnimalScreen` (register mode)
- `edit_animal/{animalId}` → `AddAnimalScreen` (edit mode, passes the animal ID)
- `animal_detail/{animalId}` → `AnimalDetailScreen`
- `report_sick` → `ReportSickScreen`
- `vaccine_calendar` → `VaccineCalendarScreen`
- `camp_alerts` → `CampAlertsScreen`
- `general_information` → `GeneralInformationScreen`

### DashboardScreen

The home screen. Displays a greeting, a summary count of animals and upcoming vaccines, and six large navigation tiles (Animal Ledger, Vaccine Calendar, Camp Alerts, Report Sick Animal, General Information, and a language toggle button).

The language toggle button calls `onLanguageToggle` which is passed down from `MainActivity` as a lambda. The lambda calls `viewModel.toggleLanguage()` and then `recreate()` to restart the Activity with the new locale.

### AnimalLedgerScreen

Lists all animals from `allAnimals`. Each animal card shows the photo, name, species, breed, and a "Due" badge if the animal has at least one pending vaccination with a `nextDueDate` within `VaccineConstants.DUE_BADGE_DAYS` days (14 days).

The "Due" badge calculation is done in the screen by collecting `allUpcomingVaccinations` and filtering for the specific `animalId`. This is a client-side join — no SQL JOIN is used.

Tapping a card navigates to `AnimalDetailScreen`. Each card has a delete button that shows a confirmation dialog before deleting.

### AddAnimalScreen (Register and Edit)

A single screen used for both registering a new animal and editing an existing one. When an `animalId` is passed via navigation, the screen loads that animal's data and pre-fills all fields.

**Form fields:**
- Name (text input)
- Photo (gallery picker via `ActivityResultContracts.GetContent()`)
- Species (dropdown — Sheep or Goat; changing species updates the breed list)
- Breed (dropdown — populated based on selected species)
- Gender (dropdown)
- Age (numeric input)
- District (dropdown — all 31 Karnataka districts, localized)
- Notes (multi-line text)
- Vaccine section — for each vaccine, two date pickers: "Last Given Date" and "Next Due Date". The farmer can fill in either or both.

On save, calls `viewModel.addAnimal(...)` or `viewModel.updateAnimal(...)` with all form data and the two vaccine maps, then navigates back.

### AnimalDetailScreen

Shows the complete health record for one animal. Loads the animal by ID from the ViewModel. Has two tabs:

**Upcoming Vaccinations tab** — lists all records where `isCompleted = false` for this animal, showing the vaccine name and next due date.

**Vaccination History tab** — lists all records where `isCompleted = true`, showing the vaccine name and the date it was administered.

An Edit button navigates to `edit_animal/{animalId}`.

### VaccineCalendarScreen

Shows all upcoming vaccinations across every animal in the herd, collected from `allUpcomingVaccinations`. Sorted by earliest due date first.

Each row shows the animal name, vaccine name, and next due date. Overdue vaccines (where `nextDueDate` is in the past) are highlighted in a distinct colour.

At the top, a seasonal campaign banner reads the current month and cross-references `VaccineConstants.SEASONAL_WINDOWS` to display which government vaccination campaigns are active that month.

### CampAlertsScreen

Shows upcoming government vaccination camps from `allCampAlerts`. Each camp card shows the title, description, location, and formatted date. A "Register Interest" button toggles the farmer's interest in a specific camp, saved to `registeredAlertIds` in `PreferenceRepository`.

A floating action button opens a dialog to add a new camp date manually (when a farmer hears about a local camp not yet in the app).

### ReportSickScreen

A simple form to report a sick animal. Fields: animal name, species, disease (dropdown from `AnimalConstants.DISEASES`), symptoms (text), and severity level (Low / Medium / High). The submit button changes colour based on severity — green for low, orange for medium, red for high.

Submitting the form shows a confirmation message simulating an alert sent to the local vet. This is fully simulated; no real network call is made.

### GeneralInformationScreen

A reference screen with static educational content. Includes sections on common livestock diseases, Karnataka-specific breed information, and general animal care guidance. Content is localised in both English and Kannada.

---

## 10. Background Workers

Both workers are implemented as `CoroutineWorker` and annotated with `@HiltWorker` so Hilt can inject DAOs into them. They are enqueued by the ViewModel using `WorkManager.enqueueUniquePeriodicWork()` with the `KEEP` policy, meaning re-enqueueing does not restart the timer.

### VaccineWorker

**File:** `worker/VaccineWorker.kt`
**Schedule:** Every 24 hours, 1-hour initial delay
**Work name:** `vaccine_reminder_periodic`

On each run:
1. Calls `vaccinationDao.getAllUpcomingVaccinations()` — all rows where `isCompleted = 0`
2. Calculates the notification window: `notifyStartTime = nextDueDate - (NOTIFICATION_DAYS * 24h)`
3. Filters for records where `now >= notifyStartTime` — this includes overdue vaccines, so a farmer is still notified even if their phone was off during the 3-day window
4. For each match, fetches the animal name via `animalDao.getAnimalById(vacc.animalId)`
5. Calculates actual days remaining and builds an appropriate message: "OVERDUE by X day(s)", "due TODAY", "due TOMORROW", or "due in X days"
6. Calls `NotificationHelper.showNotification()` with a unique notification ID calculated as `(animalId * 10000 + vaccinationId)`

Returns `Result.success()` on completion, `Result.retry()` if any exception is thrown.

### CampReminderWorker

**File:** `worker/CampReminderWorker.kt`
**Schedule:** Every 24 hours (enqueued alongside VaccineWorker)

On each run:
1. Calls `campAlertDao.getUpcomingCamps(now)` — only camps in the future
2. Filters for camps where `timeToCamp` is between 0 and 3 days (in milliseconds)
3. For each match, formats the camp date as `dd MMM yyyy` using `SimpleDateFormat`
4. Sends a notification with the camp title, location, and formatted date

---

## 11. Notifications

**File:** `notifications/NotificationHelper.kt`

Creates a notification channel named `vaccine_alerts` (channel ID) with `IMPORTANCE_HIGH`. The channel is created in the `init` block so it is always available before any notification is sent.

`showNotification(title, message, notificationId)`:
- Creates a `PendingIntent` that opens `MainActivity` when the notification is tapped
- Uses `NotificationCompat.BigTextStyle` so long messages are fully visible when the notification is expanded
- Sets `setAutoCancel(true)` so the notification dismisses itself when tapped
- Uses `PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE`

**Notification channel:** On Android 8.0 and above, a notification channel must be created before sending any notification. The channel is created via `NotificationManager.createNotificationChannel()`. If the channel already exists, calling `createNotificationChannel()` again does nothing.

**Android 13+ permission:** `POST_NOTIFICATIONS` permission is declared in the Manifest and must be granted at runtime on Android 13 and above. `MainActivity` should request this permission on first launch. (The permission request UI is shown to the user automatically by the system the first time a notification is attempted on API 33+.)

---

## 12. Language Support

The app supports two languages: English (default) and Kannada.

**Resource files:**
- `res/values/strings.xml` — English strings (used when no locale override is set)
- `res/values-kn/strings.xml` — Kannada strings (used when locale is set to `kn`)

**How locale switching works:**

1. The farmer taps the language button in `DashboardScreen`
2. The `onLanguageToggle` lambda in `MainActivity` fires
3. `viewModel.toggleLanguage()` saves the new language code (`"en"` or `"kn"`) to `SharedPreferences` key `lang` under the file `grama_vaxi_prefs`
4. `recreate()` is called, which destroys and recreates the Activity
5. `MainActivity.attachBaseContext()` runs before `setContent()`. It reads the `lang` key from `SharedPreferences`, creates a `Locale` object, and wraps the base context with `createConfigurationContext(config)`. This makes the system automatically resolve all `stringResource()` calls from the correct `values-kn/` folder

**Why `attachBaseContext` and not `CompositionLocalProvider`:** Android's resource system resolves strings from `strings.xml` at the system level based on the context's locale configuration. Simply providing a different locale in a Compose `CompositionLocal` does not affect `stringResource()` calls — those still read from the original context. The correct approach is to override the locale at the `Context` level before the Activity starts, which is what `attachBaseContext` does.

**What is localised:**
- All UI labels, buttons, dropdowns, and section headings
- District names, breed names
- Notification channel description

**What is not yet localised:**
- Vaccine name keys (e.g. "FMD Vaccine") used in the campaign banner — these come from `VaccineConstants` string constants, not from string resources
- Disease names in the Report Sick dropdown — stored as plain English strings in `AnimalConstants.DISEASES`

---

## 13. Dependency Injection

Hilt is used for dependency injection throughout the app.

**`GramaVaxiApplication`** — annotated with `@HiltAndroidApp`. Also implements `Configuration.Provider` and provides a custom `WorkManager` configuration using `HiltWorkerFactory`. This is required so Hilt can inject dependencies into `@HiltWorker` classes (VaccineWorker and CampReminderWorker).

**`DatabaseModule`** (`di/DatabaseModule.kt`) — a `@Singleton` `@Module` installed in `SingletonComponent`. Provides:
- `AppDatabase` — built with `Room.databaseBuilder()`
- `AnimalDao` — from `database.animalDao()`
- `VaccinationDao` — from `database.vaccinationDao()`
- `CampAlertDao` — from `database.campAlertDao()`

**`RepositoryModule`** (`di/RepositoryModule.kt`) — provides:
- `AnimalRepository` — binds the interface to `AnimalRepositoryImpl`
- `PreferenceRepository` — binds the interface to `PreferenceRepositoryImpl`

**`AnimalViewModel`** — annotated with `@HiltViewModel`. Injected into composables via `hiltViewModel()`.

---

## 14. Dependencies and Libraries

All dependencies are declared in `app/build.gradle.kts`. Version catalogs are managed in `gradle/libs.versions.toml`.

| Library | Purpose |
|---|---|
| **Jetpack Compose BOM** | UI framework — all Compose libraries versioned together |
| **Material3** | UI components (cards, buttons, dialogs, dropdowns, chips) |
| **Compose Navigation** | Screen-to-screen navigation within a single Activity |
| **Room Runtime + KTX** | Local SQLite database with Kotlin coroutines support |
| **Room Compiler (KSP)** | Generates DAO implementation code at compile time |
| **WorkManager KTX** | Background task scheduling that survives reboots and battery optimization |
| **Hilt Android** | Dependency injection container |
| **Hilt Compiler (KSP)** | Generates Hilt injection code at compile time |
| **Hilt Work** | Integration between Hilt and WorkManager for injecting into workers |
| **Hilt Navigation Compose** | `hiltViewModel()` function for injecting ViewModels into composables |
| **Coil Compose** | Image loading — displays animal photos from file paths |
| **Lifecycle ViewModel Compose** | ViewModel integration with Compose |
| **Core KTX** | Kotlin extensions for Android core APIs |
| **Activity Compose** | `setContent {}` and Compose Activity integration |

**Build tooling:**
- KSP (Kotlin Symbol Processing) — used instead of KAPT for Room and Hilt code generation. KSP is significantly faster than KAPT.
- Compose Compiler plugin — required for all Compose projects

---

## 15. Known Limitations

**Vet alert is simulated**(as specified in the project description from mindmatrix) — The "Report Sick Animal" form submits data but does not send it anywhere. It shows a local success message only. A real implementation would require a backend server or SMS gateway. 

**Camp alerts are manual** — There is no integration with any government API or feed. Camp dates must be entered manually into the app. A future version could pull scheduled camps from the Karnataka Animal Husbandry Department's systems.

**No multi-user support** — The app is designed for a single farmer on a single device. A shared village-level herd database would require a backend and authentication system.

**Language toggle race condition** — `PreferenceRepositoryImpl.setLanguage()` uses `.apply()` (asynchronous write). When `recreate()` is called immediately after `toggleLanguage()`, there is a small chance `attachBaseContext` runs before the write completes and reads the old language. Using `.commit()` (synchronous write) would eliminate this race.