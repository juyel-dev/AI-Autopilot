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
    private fun maskSecret(secret: String): String {
        if (secret.isBlank()) return ""
        if (secret.length <= 6) return "****"
        return secret.take(4) + "****" + secret.takeLast(4)
    }

    private fun maskUrl(url: String): String {
        if (url.isBlank()) return ""
        val protocolEnd = url.indexOf("://")
        if (protocolEnd != -1) {
            val start = url.substring(0, protocolEnd + 3)
            return start + "****"
        }
        return "****"
    }

    // App Settings
    val appSettingsFlow: Flow<AppSettings?> = appSettingsDao.getSettingsFlow().map { settings ->
        settings?.copy(
            supabaseUrl = maskUrl(secureStorage.getString("supabaseUrl", "")),
            anonKey = maskSecret(secureStorage.getString("anonKey", "")),
            serviceRoleKey = maskSecret(secureStorage.getString("serviceRoleKey", "")),
            patKey = maskSecret(secureStorage.getString("patKey", "")),
            facebookToken = maskSecret(secureStorage.getString("facebookToken", "")),
            aiApiKey = maskSecret(secureStorage.getString("aiApiKey", "")),
            imageApiKey = maskSecret(secureStorage.getString("imageApiKey", ""))
        )
    }

    suspend fun getSettingsDirect(): AppSettings? {
        val settings = appSettingsDao.getSettingsDirect() ?: return null

        var needsMigration = false
        if (settings.supabaseUrl.isNotBlank()) { secureStorage.saveString("supabaseUrl", settings.supabaseUrl); needsMigration = true }
        if (settings.anonKey.isNotBlank()) { secureStorage.saveString("anonKey", settings.anonKey); needsMigration = true }
        if (settings.serviceRoleKey.isNotBlank()) { secureStorage.saveString("serviceRoleKey", settings.serviceRoleKey); needsMigration = true }
        if (settings.patKey.isNotBlank()) { secureStorage.saveString("patKey", settings.patKey); needsMigration = true }
        if (settings.facebookToken.isNotBlank()) { secureStorage.saveString("facebookToken", settings.facebookToken); needsMigration = true }
        if (settings.aiApiKey.isNotBlank()) { secureStorage.saveString("aiApiKey", settings.aiApiKey); needsMigration = true }
        if (settings.imageApiKey.isNotBlank()) { secureStorage.saveString("imageApiKey", settings.imageApiKey); needsMigration = true }

        if (needsMigration) {
            appSettingsDao.saveSettings(settings.copy(
                supabaseUrl = "", anonKey = "", serviceRoleKey = "", patKey = "",
                facebookToken = "", aiApiKey = "", imageApiKey = ""
            ))
        }

        return settings.copy(
            supabaseUrl = maskUrl(secureStorage.getString("supabaseUrl", "")),
            anonKey = maskSecret(secureStorage.getString("anonKey", "")),
            serviceRoleKey = maskSecret(secureStorage.getString("serviceRoleKey", "")),
            patKey = maskSecret(secureStorage.getString("patKey", "")),
            facebookToken = maskSecret(secureStorage.getString("facebookToken", "")),
            aiApiKey = maskSecret(secureStorage.getString("aiApiKey", "")),
            imageApiKey = maskSecret(secureStorage.getString("imageApiKey", ""))
        )
    }

    suspend fun getSettingsRaw(): AppSettings? {
        val settings = appSettingsDao.getSettingsDirect() ?: return null
        return settings.copy(
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
        if (!settings.supabaseUrl.contains("****")) secureStorage.saveString("supabaseUrl", settings.supabaseUrl)
        if (!settings.anonKey.contains("****")) secureStorage.saveString("anonKey", settings.anonKey)
        if (!settings.serviceRoleKey.contains("****")) secureStorage.saveString("serviceRoleKey", settings.serviceRoleKey)
        if (!settings.patKey.contains("****")) secureStorage.saveString("patKey", settings.patKey)
        if (!settings.facebookToken.contains("****")) secureStorage.saveString("facebookToken", settings.facebookToken)
        if (!settings.aiApiKey.contains("****")) secureStorage.saveString("aiApiKey", settings.aiApiKey)
        if (!settings.imageApiKey.contains("****")) secureStorage.saveString("imageApiKey", settings.imageApiKey)

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
