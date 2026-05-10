package com.example.ourgramavaxi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ourgramavaxi.R

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val species: String,
    val breed: String,
    val gender: String,
    val ageInYears: Int,
    val district: String = "Mandya",
    val notes: String = "",
    val photoUri: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

object AnimalConstants {
    val SPECIES = listOf("Sheep", "Goat")
    val GENDERS = listOf("Male", "Female")

    val DISTRICTS = listOf<Pair<String, Int>>(
        "Bagalkot" to R.string.bagalkot,
        "Ballari" to R.string.ballari,
        "Belagavi" to R.string.belagavi,
        "Bengaluru Rural" to R.string.bengaluru_rural,
        "Bengaluru Urban" to R.string.bengaluru_urban,
        "Bidar" to R.string.bidar,
        "Chamarajanagar" to R.string.chamarajanagar,
        "Chikkaballapur" to R.string.chikkaballapur,
        "Chikkamagaluru" to R.string.chikkamagaluru,
        "Chitradurga" to R.string.chitradurga,
        "Dakshina Kannada" to R.string.dakshina_kannada,
        "Davanagere" to R.string.davanagere,
        "Dharwad" to R.string.dharwad,
        "Gadag" to R.string.gadag,
        "Hassan" to R.string.hassan,
        "Haveri" to R.string.haveri,
        "Kalaburagi" to R.string.kalaburagi,
        "Kodagu" to R.string.kodagu,
        "Kolar" to R.string.kolar,
        "Koppal" to R.string.koppal,
        "Mandya" to R.string.mandya,
        "Mysuru" to R.string.mysuru,
        "Raichur" to R.string.raichur,
        "Ramanagara" to R.string.ramanagara,
        "Shivamogga" to R.string.shivamogga,
        "Tumakuru" to R.string.tumakuru,
        "Udupi" to R.string.udupi,
        "Uttara Kannada" to R.string.uttara_kannada,
        "Vijayapura" to R.string.vijayapura,
        "Yadgir" to R.string.yadgir,
        "Vijayanagara" to R.string.vijayanagara
    )

    val SHEEP_BREEDS = listOf<Pair<String, Int>>(
        "Deccani" to R.string.deccani,
        "Bellary" to R.string.bellary,
        "Mandya" to R.string.mandya,
        "Bannur" to R.string.bannur,
        "Hassan" to R.string.hassan,
        "Others" to R.string.others
    )
    val GOAT_BREEDS = listOf<Pair<String, Int>>(
        "Osmanabadi" to R.string.osmanabadi,
        "Bidri" to R.string.bidri,
        "Beetal" to R.string.beetal,
        "Kodagu" to R.string.kodagu_breed,
        "Others" to R.string.others
    )

    // ✅ BUG 5 FIX: Diseases now use (key, resId) pairs just like SHEEP_BREEDS and GOAT_BREEDS
    // so they can be properly localized in Kannada
    val DISEASES = listOf<Pair<String, Int>>(
        "Foot & Mouth Disease"             to R.string.disease_fmd,
        "Sheep & Goat Plague (PPR)"        to R.string.disease_ppr,
        "Goat Pox"                         to R.string.disease_goat_pox,
        "Sheep Pox"                        to R.string.disease_sheep_pox,
        "Haemorrhagic Septicaemia (HS)"    to R.string.disease_hs,
        "Others"                           to R.string.disease_others
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

    val VACCINE_INTERVALS = mapOf(
        FMD to 180,
        ENTEROTOXEMIA to 180,
        PPR to 1095,
        POX to 365,
        BLUETONGUE to 365,
        CCPP to 365,
        HS to 365,
        ANTHRAX to 365
    )

    val SEASONAL_WINDOWS = mapOf(
        FMD to listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
        HS to listOf(4, 5),
        POX to listOf(1, 2),
        ANTHRAX to listOf(7, 8)
    )

    val HOTSPOT_ZONES = mapOf(
        ANTHRAX to listOf("Chamarajanagar", "Mandya", "Mysuru")
    )

    // ✅ Bug 13 fix: Named constants instead of magic numbers scattered across files
    const val DUE_BADGE_DAYS = 14L      // Show "Due" badge in Animal Ledger
    const val NOTIFICATION_DAYS = 3L    // Send push notification
}