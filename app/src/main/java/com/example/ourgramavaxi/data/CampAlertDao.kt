package com.example.ourgramavaxi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CampAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampAlert(campAlert: CampAlert)

    @Query("SELECT * FROM camp_alerts ORDER BY date ASC")
    fun getAllCampAlerts(): Flow<List<CampAlert>>

    @Query("SELECT * FROM camp_alerts WHERE date >= :currentTime ORDER BY date ASC")
    fun getUpcomingCamps(currentTime: Long): Flow<List<CampAlert>>

    @Delete
    suspend fun deleteCampAlert(campAlert: CampAlert)

    @Query("DELETE FROM camp_alerts WHERE date < :cutoffTime")
    suspend fun deletePastCamps(cutoffTime: Long)
}