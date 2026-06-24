package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AetherRepository(
    private val appSettingsDao: AppSettingsDao,
    private val contentBriefDao: ContentBriefDao,
    private val systemEventDao: SystemEventDao,
    private val costEntryDao: CostEntryDao,
    private val engagementSnapshotDao: EngagementSnapshotDao,
    private val secureStorage: SecureStorage
) {
    // App Settings
    val appSettingsFlow: Flow<AppSettings?> = appSettingsDao.getSettingsFlow().map { settings ->
        settings?.copy(
            supabaseUrl = secureStorage.getString("supabaseUrl", ""),
            anonKey = secureStorage.getString("anonKey", ""),
            serviceRoleKey = secureStorage.getString("serviceRoleKey", ""),
            patKey = secureStorage.getString("patKey", ""),
            facebookToken = secureStorage.getString("facebookToken", ""),
            aiApiKey = secureStorage.getString("aiApiKey", ""),
            imageApiKey = secureStorage.getString("imageApiKey", "")
        )
    }

    suspend fun getSettingsDirect(): AppSettings? {
        val settings = appSettingsDao.getSettingsDirect()
        return settings?.copy(
            supabaseUrl = secureStorage.getString("supabaseUrl", ""),
            anonKey = secureStorage.getString("anonKey", ""),
            serviceRoleKey = secureStorage.getString("serviceRoleKey", ""),
            patKey = secureStorage.getString("patKey", ""),
            facebookToken = secureStorage.getString("facebookToken", ""),
            aiApiKey = secureStorage.getString("aiApiKey", ""),
            imageApiKey = secureStorage.getString("imageApiKey", "")
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        secureStorage.saveString("supabaseUrl", settings.supabaseUrl)
        secureStorage.saveString("anonKey", settings.anonKey)
        secureStorage.saveString("serviceRoleKey", settings.serviceRoleKey)
        secureStorage.saveString("patKey", settings.patKey)
        secureStorage.saveString("facebookToken", settings.facebookToken)
        secureStorage.saveString("aiApiKey", settings.aiApiKey)
        secureStorage.saveString("imageApiKey", settings.imageApiKey)

        // Save safely scrubbed settings to DB
        appSettingsDao.saveSettings(
            settings.copy(
                supabaseUrl = "",
                anonKey = "",
                serviceRoleKey = "",
                patKey = "",
                facebookToken = "",
                aiApiKey = "",
                imageApiKey = ""
            )
        )
    }

    // Content Briefs
    val allBriefsFlow: Flow<List<ContentBrief>> = contentBriefDao.getAllBriefsFlow()
    suspend fun getBriefById(id: Long): ContentBrief? = contentBriefDao.getBriefById(id)
    suspend fun insertBrief(brief: ContentBrief): Long = contentBriefDao.insertBrief(brief)
    suspend fun updateBrief(brief: ContentBrief) = contentBriefDao.updateBrief(brief)
    suspend fun deleteBriefById(id: Long) = contentBriefDao.deleteBriefById(id)
    suspend fun clearAllBriefs() = contentBriefDao.clearAllBriefs()

    // System Events
    val recentEventsFlow: Flow<List<SystemEvent>> = systemEventDao.getRecentEventsFlow()
    suspend fun insertEvent(category: String, severity: String, message: String): Long {
        return systemEventDao.insertEvent(SystemEvent(category = category, severity = severity, message = message))
    }
    suspend fun clearLog() = systemEventDao.clearLog()

    // Cost Management
    val costEntriesFlow: Flow<List<CostEntry>> = costEntryDao.getCostEntriesFlow()
    val totalCostFlow: Flow<Double?> = costEntryDao.getTotalCostFlow()
    suspend fun insertCost(provider: String, model: String, inputTokens: Int, outputTokens: Int, estimatedCost: Double): Long {
        return costEntryDao.insertCost(
            CostEntry(
                provider = provider,
                model = model,
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                estimatedCost = estimatedCost
            )
        )
    }
    suspend fun clearCosts() = costEntryDao.clearCosts()

    // Engagement Snapshots
    val snapshotsFlow: Flow<List<EngagementSnapshot>> = engagementSnapshotDao.getSnapshotsFlow()
    suspend fun insertSnapshot(snapshot: EngagementSnapshot): Long = engagementSnapshotDao.insertSnapshot(snapshot)
    suspend fun clearSnapshots() = engagementSnapshotDao.clearSnapshots()
}
