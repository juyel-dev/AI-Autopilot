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

        val settings = repository.getSettingsDirect() ?: AppSettings()
        
        // If system hasn't completed setup or is in purely manual mode, stop
        if (!settings.isSetupComplete || settings.postingMode == "manual") {
            return Result.success()
        }

        val now = System.currentTimeMillis()
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
