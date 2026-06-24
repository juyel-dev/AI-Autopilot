package com.example.scheduler

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.*
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

class PublishWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val client = OkHttpClient()

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val secureStorage = SecureStorage(applicationContext)
        val repository = AetherRepository(
            database.appSettingsDao(),
            database.contentBriefDao(),
            database.systemEventDao(),
            database.costEntryDao(),
            database.engagementSnapshotDao(),
            secureStorage
        )

        val settings = repository.getSettingsRaw() ?: AppSettings()
        
        if (!settings.isSetupComplete) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        
        // 1. PERFORM SUPABASE SYNCHRONIZATION CYCLES
        val supabaseUrl = settings.supabaseUrl.trim()
        val supabaseKey = if (settings.serviceRoleKey.trim().isNotEmpty()) settings.serviceRoleKey.trim() else settings.anonKey.trim()

        if (supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
            try {
                repository.insertEvent(
                    category = "scheduler",
                    severity = "info",
                    message = "Starting background synchronization with Supabase backend..."
                )

                // A. Pull Remote Briefs
                val remoteBriefs = com.example.api.SupabaseService.pullBriefs(supabaseUrl, supabaseKey)
                if (remoteBriefs.isNotEmpty()) {
                    val localBriefs = repository.allBriefsFlow.first()
                    val localMap = localBriefs.associateBy { it.id }
                    
                    for (remote in remoteBriefs) {
                        val local = localMap[remote.id]
                        if (local == null) {
                            repository.insertBrief(remote)
                        } else if (local.status != remote.status || local.isApproved != remote.isApproved || local.slotTime != remote.slotTime) {
                            repository.updateBrief(remote)
                        }
                    }
                }

                // B. Push Local Briefs
                val currentBriefs = repository.allBriefsFlow.first()
                if (currentBriefs.isNotEmpty()) {
                    com.example.api.SupabaseService.pushBriefs(supabaseUrl, supabaseKey, currentBriefs)
                }

                // C. Push Local Events
                val recentEvents = repository.recentEventsFlow.first()
                if (recentEvents.isNotEmpty()) {
                    com.example.api.SupabaseService.pushSystemEvents(supabaseUrl, supabaseKey, recentEvents)
                }

                // D. Push Cost Entries & Snapshots
                val costEntries = repository.costEntriesFlow.first()
                if (costEntries.isNotEmpty()) {
                    com.example.api.SupabaseService.pushCostEntries(supabaseUrl, supabaseKey, costEntries)
                }

                val snapshots = repository.snapshotsFlow.first()
                if (snapshots.isNotEmpty()) {
                    com.example.api.SupabaseService.pushEngagementSnapshots(supabaseUrl, supabaseKey, snapshots)
                }

                repository.insertEvent(
                    category = "scheduler",
                    severity = "info",
                    message = "Supabase background synchronization completed successfully."
                )
            } catch (e: Exception) {
                repository.insertEvent(
                    category = "scheduler",
                    severity = "warn",
                    message = "Supabase sync cycle warning: ${e.localizedMessage}"
                )
            }
        }

        if (settings.postingMode == "manual") {
            return Result.success()
        }

        try {
            // Get all briefs from the Flow
            val allBriefs = repository.allBriefsFlow.first()
            
            // Find briefs that are approved, not yet published/skipped, and past scheduled time
            val pendingPublishList = allBriefs.filter { 
                it.status == "approved" && it.isApproved && it.slotTime <= now 
            }

            if (pendingPublishList.isEmpty()) {
                return Result.success()
            }

            for (brief in pendingPublishList) {
                repository.insertEvent(
                    category = "publish",
                    severity = "info",
                    message = "Automatic scheduler processing Brief #${brief.id} ('${brief.topic}')..."
                )

                val token = settings.facebookToken.trim()
                val pageId = settings.facebookPageId.trim()

                var publishSuccess = false
                var systemMessage = ""

                if (token.isNotEmpty() && pageId.isNotEmpty()) {
                    // Make real API Call
                    publishSuccess = try {
                        val requestUrl = if (brief.imageUrl.isNotEmpty()) {
                            "https://graph.facebook.com/v19.0/$pageId/photos"
                        } else {
                            "https://graph.facebook.com/v19.0/$pageId/feed"
                        }

                        val jsonBody = JSONObject().apply {
                            put("message", "${brief.topic}\n\n${brief.caption}\n\n${brief.hashtags.split(",").joinToString(" ") { "#$it" }}")
                            if (brief.imageUrl.isNotEmpty()) {
                                put("url", brief.imageUrl)
                            }
                        }

                        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                        val request = Request.Builder()
                            .url(requestUrl)
                            .post(requestBody)
                            .header("Authorization", "Bearer $token")
                            .build()

                        client.newCall(request).execute().use { response ->
                            val bodyText = response.body?.string() ?: ""
                            if (response.isSuccessful) {
                                systemMessage = "Published to Facebook successfully. Response Code: ${response.code}."
                                true
                            } else {
                                systemMessage = "Facebook API rejected request (HTTP ${response.code})"
                                false
                            }
                        }
                    } catch (e: Exception) {
                        systemMessage = "Network failure during Facebook API dispatch"
                        false
                    }
                } else {
                    systemMessage = "Facebook token or page ID is missing. Cannot publish."
                }

                if (publishSuccess) {
                    // Update brief status
                    repository.updateBrief(brief.copy(status = "published"))

                    // Log real event
                    repository.insertEvent(
                        category = "publish",
                        severity = "info",
                        message = "✅ Post '${brief.topic}' successfully published. $systemMessage"
                    )

                    // Initialize analytics snapshot for the post
                    val labelFormat = SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(now))
                    
                    repository.insertSnapshot(
                        EngagementSnapshot(
                            label = labelFormat,
                            likes = 0,
                            comments = 0,
                            shares = 0,
                            reach = 0,
                            timestamp = now
                        )
                    )
                } else {
                    // Log fail
                    repository.insertEvent(
                        category = "publish",
                        severity = "error",
                        message = "❌ Automatic scheduler stalled for Content ID #${brief.id}: $systemMessage"
                    )
                }
            }
        } catch (e: Exception) {
            repository.insertEvent(
                category = "scheduler",
                severity = "error",
                message = "Background scheduler encountered fatal exception during execution."
            )
        }

        return Result.success()
    }
}
