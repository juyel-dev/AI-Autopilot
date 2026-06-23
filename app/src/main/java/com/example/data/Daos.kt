package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)
}

@Dao
interface ContentBriefDao {
    @Query("SELECT * FROM content_briefs ORDER BY slotTime ASC")
    fun getAllBriefsFlow(): Flow<List<ContentBrief>>

    @Query("SELECT * FROM content_briefs WHERE id = :id LIMIT 1")
    suspend fun getBriefById(id: Long): ContentBrief?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrief(brief: ContentBrief): Long

    @Update
    suspend fun updateBrief(brief: ContentBrief)

    @Query("DELETE FROM content_briefs WHERE id = :id")
    suspend fun deleteBriefById(id: Long)

    @Query("DELETE FROM content_briefs")
    suspend fun clearAllBriefs()
}

@Dao
interface SystemEventDao {
    @Query("SELECT * FROM system_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEventsFlow(): Flow<List<SystemEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SystemEvent): Long

    @Query("DELETE FROM system_events")
    suspend fun clearLog()
}

@Dao
interface CostEntryDao {
    @Query("SELECT * FROM cost_entries ORDER BY timestamp DESC")
    fun getCostEntriesFlow(): Flow<List<CostEntry>>

    @Query("SELECT SUM(estimatedCost) FROM cost_entries")
    fun getTotalCostFlow(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCost(cost: CostEntry): Long

    @Query("DELETE FROM cost_entries")
    suspend fun clearCosts()
}

@Dao
interface EngagementSnapshotDao {
    @Query("SELECT * FROM engagement_snapshots ORDER BY timestamp ASC")
    fun getSnapshotsFlow(): Flow<List<EngagementSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: EngagementSnapshot): Long

    @Query("DELETE FROM engagement_snapshots")
    suspend fun clearSnapshots()
}
