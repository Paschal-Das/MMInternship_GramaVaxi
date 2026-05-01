package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ourgramavaxi.R

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val species: String, // "Sheep" or "Goat"
    val breed: String,
    val gender: String, // "Male" or "Female"
    val ageInYears: Int,
    val district: String = "Mandya", // Default for prototype
    val photoUri: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

object AnimalConstants {
    val SPECIES = listOf("Sheep", "Goat")
    val GENDERS = listOf("Male", "Female")

    val DISTRICTS = listOf(
        "Mandya" to R.string.mandya,
        "Hassan" to R.string.hassan,
        "Chamarajanagar" to R.string.chamarajanagar,
        "Mysuru" to R.string.mysuru,
        "Tumakuru" to R.string.tumakuru,
        "Bengaluru Rural" to R.string.bengaluru_rural,
        "Chikkaballapura" to R.string.chikkaballapura,
        "Kolar" to R.string.kolar
    )
    
    val SHEEP_BREEDS = listOf(
        "Deccani" to R.string.deccani,
        "Bellary" to R.string.bellary,
        "Mandya" to R.string.mandya,
        "Bannur" to R.string.bannur,
        "Hassan" to R.string.hassan,
        "Others" to R.string.others
    )
    val GOAT_BREEDS = listOf(
        "Osmanabadi" to R.string.osmanabadi,
        "Bidri" to R.string.bidri,
        "Malnad Gidda" to R.string.malnad_gidda,
        "Beetal" to R.string.beetal,
        "Kodagu" to R.string.kodagu,
        "Others" to R.string.others
    )

    val DISEASES = listOf(
        "Foot & Mouth Disease",
        "Sheep & Goat Plague (PPR)",
        "Goat Pox",
        "Sheep Pox",
        "Haemorrhagic Septicaemia (HS)",
        "Others"
    )
}

object VaccineConstants {
    const val FMD = "FMD Vaccine"
    const val PPR = "PPR Vaccine"
    const val POX = "Sheep/Goat Pox Vaccine"
    const val HS = "HS Vaccine"
    const val BLUETONGUE = "Bluetongue Vaccine"
    const val ENTEROTOXEMIA = "Enterotoxemia"
    const val CCPP = "CCPP (Goat)"
    const val ANTHRAX = "Anthrax Vaccine"

    // Intervals in days
    val VACCINE_INTERVALS = mapOf(
        FMD to 180,           // 6 months
        ENTEROTOXEMIA to 180, // 6 months
        PPR to 1095,          // 3 years
        POX to 365,           // 12 months
        BLUETONGUE to 365,    // 12 months
        CCPP to 365,          // 12 months
        HS to 365,            // 12 months
        ANTHRAX to 365        // 12 months
    )

    // Seasonal Windows (Month indices: 0 = Jan, 1 = Feb, etc.)
    val SEASONAL_WINDOWS = mapOf(
        FMD to listOf(3, 9),      // April (3) & October (9)
        HS to listOf(4, 5),       // May (4) & June (5) - Pre-monsoon
        POX to listOf(1, 2),      // Feb (1) & March (2) - Pre-summer
        ANTHRAX to listOf(7, 8)   // Aug (7) & Sept (8) - Post-monsoon windows
    )

    // Districts where specific vaccines are mandatory/highly recommended
    val HOTSPOT_ZONES = mapOf(
        ANTHRAX to listOf("Chamarajanagar", "Mandya", "Mysuru")
    )
}
