package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val projectName: String = "Aether Space",
    val supabaseUrl: String = "",
    val anonKey: String = "",
    val serviceRoleKey: String = "",
    val patKey: String = "",
    val projectRef: String = "",
    val dbEnableMigrations: Boolean = true,
    val dbSchemaSetup: Boolean = true,
    val storageBucketName: String = "media",
    val storageIsPublic: Boolean = true,
    val facebookToken: String = "",
    val facebookPageId: String = "",
    val aiApiKey: String = "",
    val aiProvider: String = "Gemini", // "OpenAI", "Gemini", "Claude", "Groq", "Custom API"
    val aiBaseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
    val aiModel: String = "gemini-2.5-flash",
    val imageProvider: String = "pollinations", // "pollinations", "openai_compatible"
    val imageBaseUrl: String = "https://image.pollinations.ai",
    val imageModel: String = "flux",
    val imageApiKey: String = "",
    val brandVoice: String = "Tech Innovator / Engaging Developer",
    val brandTone: String = "Professional, Informative",
    val brandTopics: String = "Tech Innovations, AI Design Aesthetics",
    val audience: String = "Indie Hackers & Creators",
    val postingMode: String = "hybrid", // "manual", "hybrid", "full_auto"
    val maxPostsPerDay: Int = 2,
    val isSetupComplete: Boolean = false,
    val dailySpendCap: Double = 5.0
) : Serializable

@Entity(tableName = "content_briefs")
data class ContentBrief(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pageId: String = "default_page",
    val slotTime: Long, // Epoch millis
    val topic: String,
    val caption: String,
    val hashtags: String, // Comma-separated
    val imagePrompt: String,
    val imageUrl: String = "", // File path or URL
    val status: String = "draft", // "draft", "approved", "published", "skipped"
    val isApproved: Boolean = false,
    val predictedScore: Double = 0.82
) : Serializable

@Entity(tableName = "system_events")
data class SystemEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "auth", "ai", "publish", "scheduler"
    val severity: String, // "info", "warn", "error"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "cost_entries")
data class CostEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String,
    val model: String,
    val inputTokens: Int,
    val outputTokens: Int,
    val estimatedCost: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "engagement_snapshots")
data class EngagementSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String, // e.g. "Mon", "Tue" or "Week 1"
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val reach: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
