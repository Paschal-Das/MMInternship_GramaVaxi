# 🐑 Grama-Vaxi — Livestock Health Alert App

> **"Because a farmer's flock is their savings account."**

Grama-Vaxi is an Android app that helps rural farmers in Karnataka track the vaccination health of their sheep and goats. It sends timely push notifications before government vaccination camps arrive at the village — even when the app hasn't been opened in days.

---

## 📖 The Problem It Solves

Livestock in Indian villages often die from preventable disease outbreaks. Government vaccination camps are announced only through local loudspeakers — a system that is easy to miss. Farmers lose their animals not from lack of care, but from lack of timely information.

Grama-Vaxi digitises this gap. It gives every animal a health record, automatically calculates the next vaccination date, and alerts the farmer **3 days before a camp reaches the village** — directly on their phone, even in offline conditions.

---

## ✨ Features

### 🐐 Animal Ledger
- Register each sheep or goat with a name, photo, breed, species, gender, age, and district
- Supports all major Karnataka breeds — Deccani, Bellary, Bannur, Osmanabadi, Bidri, Malnad Gidda, and more
- Visual "Due" badge on any animal whose vaccine is coming up within 14 days
- Delete animal with a confirmation dialog to prevent accidental removal

### 💉 Vaccine Calendar
- Automatically generates the next vaccination due date based on standard government intervals
- Displays all upcoming doses across all animals, sorted by earliest first
- Highlights overdue vaccines in red so the farmer knows what is urgent
- Shows a seasonal campaign banner when the government is actively running drives for specific vaccines that month

### 🏕️ Camp Alerts
- Lists upcoming government vaccination camps with location, date, and description
- Farmer can register interest in a camp with one tap
- Supports adding new camp dates manually when announced locally
- Past camps are automatically hidden so the screen stays clean

### 🤒 Report Sick Animal
- Quick form to report a sick animal with symptoms, known disease, and severity level
- Simulates sending an alert to the local veterinarian
- Severity level changes the button colour — red for high urgency

### 🌐 Kannada Language Support
- Full bilingual support — English and Kannada
- Switch languages instantly with one tap from the home screen
- All labels, dropdowns, vaccine names, notifications, and messages are localised

### 🔔 Background Notifications
- Vaccine reminders fire even when the app has not been opened for days
- Camp arrival alerts fire 3 days before the scheduled camp date
- Overdue vaccines continue to notify the farmer until the record is updated
- Notifications include the animal's name, vaccine name, and exact days remaining

---

## 🏗️ Technical Architecture

Grama-Vaxi is built with a clean, layered Android architecture suitable for offline-first rural environments where internet connectivity is unreliable.

| Layer | Technology |
|---|---|
| UI | Jetpack Compose |
| Navigation | Compose Navigation |
| State Management | ViewModel + StateFlow |
| Database | Room (SQLite) |
| Background Work | WorkManager |
| Dependency Injection | Hilt |
| Image Loading | Coil |
| Language Preferences | SharedPreferences + Activity recreate |

### Key Design Decisions

**Offline-first** — Room stores all animal and vaccination data locally. The app works completely without internet. No user data ever leaves the device.

**WorkManager for notifications** — WorkManager is used instead of simple alarms because it survives app restarts, device reboots, and battery optimisation. Vaccine reminders are scheduled as periodic work that runs every 24 hours, regardless of whether the farmer opens the app.

**Separation of concerns** — Business logic for registering and updating animals lives in dedicated `UseCase` classes, keeping the ViewModel thin and the logic testable.

**Two-record vaccination model** — Each vaccine entry creates two separate database records: one completed history record (shown in the animal's health timeline) and one pending upcoming record (shown in the calendar and read by the notification worker). This ensures the calendar and notifications always have clean, unambiguous data to work with.

---

## 📱 App Flow

```
Dashboard
├── Animal Ledger
│   ├── Add Animal (photo, breed, district, vaccine history)
│   ├── Animal Detail (health timeline, upcoming doses)
│   └── Edit Animal
├── Vaccine Calendar (all upcoming doses across all animals)
├── Camp Alerts (upcoming government camps, register interest)
├── Report Sick Animal (simulated vet alert)
└── General Information (disease guides, breed info)
```

---

## 🗃️ Database Schema

**animals** — Stores each registered animal with all profile fields and an optional photo path.

**vaccinations** — Stores vaccination records. Each record is either a completed history entry (`isCompleted = true`) or a pending upcoming reminder (`isCompleted = false`). The calendar and workers only read pending records.

**camp_alerts** — Stores upcoming government vaccination camps with title, location, date, and type.

---

## 🐛 Known Limitations

- The "Report Sick" feature is simulated — it does not connect to a real veterinarian network. This is a placeholder for a future real-time vet alert integration.
- The app does not sync data across devices. Each phone holds its own local copy of the herd record.
- Camp alerts must be added manually by someone in the village with the app. Future versions could pull camp schedules from a government API.

---

## 🛠️ Setup & Build

**Prerequisites**
- Android Studio Hedgehog or later
- Kotlin 1.9+
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 35

**Clone and run**

1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync
4. Run on a device or emulator running Android 8.0 or above

No API keys or external services are required. The app runs entirely offline.

---

## 📋 Permissions Used

| Permission | Reason |
|---|---|
| `POST_NOTIFICATIONS` | Show vaccine and camp reminder notifications |
| `READ_MEDIA_IMAGES` | Pick animal photos from the gallery (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Pick animal photos from the gallery (Android 12 and below) |

---

## 🎯 Impact Goals

- **Livestock Wealth** — Preventing animal loss protects the primary savings and income source for rural farming families
- **Animal Welfare** — Ensuring every sheep and goat in the village receives timely, scheduled medical care
- **Health Digitisation** — Building a village-level database of animal health records that can inform future policy and outreach

---

## 🗺️ Supported Districts

Grama-Vaxi covers all 31 districts of Karnataka, with special hotspot alerts for districts with historically high disease risk — including Anthrax-prone zones around Chamarajanagar, Mandya, and Mysuru.

---

## 🌱 Future Scope

- Real vet alert integration via SMS or a lightweight backend
- Government camp schedule sync from Karnataka Animal Husbandry Department
- Herd-level health reports for the village Panchayat
- Multi-farmer support — one phone for the entire village
- Voice input for farmers who are not comfortable with text

---

## 👨‍💻 Built By

Built as part of a student Android development project focused on using technology to solve real problems in rural Karnataka.

---

## 📄 License

This project is open source. See the LICENSE file for details.
