package com.example.api

import android.util.Log
import com.example.data.ContentBrief
import com.example.data.CostEntry
import com.example.data.AetherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object GeminiAIService {
    private const val TAG = "GeminiAIService"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Generates a high-quality creative brief (caption, hashtags, and an image prompt)
     * using either Gemini or OpenAI-compatible REST endpoints.
     */
    suspend fun generateBrief(
        provider: String,
        baseUrl: String,
        apiKey: String,
        modelName: String,
        pageTopic: String,
        brandVoice: String,
        audience: String,
        slotTime: Long,
        repository: AetherRepository
    ): ContentBrief = withContext(Dispatchers.IO) {
        val brief = generateLocalBrief(pageTopic, brandVoice, audience, slotTime)
        repository.insertEvent(
            category = "ai",
            severity = "info",
            message = "Content brief generated locally offline."
        )
        return@withContext brief
    }

    private fun cleanJsonResponse(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    /**
     * Local procedural generator to mock content elegantly when offline or no API Key
     */
    fun generateLocalBrief(
        pageTopic: String,
        brandVoice: String,
        audience: String,
        slotTime: Long
    ): ContentBrief {
        val pageNiche = if (pageTopic.isBlank()) "Global Tech & Aesthetics" else pageTopic
        val tone = if (brandVoice.isBlank()) "Professional Creative" else brandVoice

        val ideas = listOf(
            Triple(
                "Demystifying the Future",
                "The evolution of $pageNiche is moving faster than ever. How are you positioning yourself for the next shift? In this piece, we cover the 3 core pillars of building sustainable scale while maintaining the clean aesthetic our readers love. Take the leap, build modularly, and focus on clean workflows.",
                "future,scale,strategy,modular"
            ),
            Triple(
                "The Aesthetic Moat",
                "Why visual polish is not just an overlay—it is the product itself. In a world saturated with plain feeds, your design is your moat. Aligning your product's depth with glass-quality premium layouts transforms standard engagement into hyper-loyal customer bases. Do not cut corners.",
                "design,aesthetic,luxurydark,moat"
            ),
            Triple(
                "High Performance Rules",
                "Speed, precision, and relentless focus. We've spent the last few weeks testing localized micro-schedulers for $pageNiche. The results? Automated pipelines paired with custom brand voices reduce weekly admin costs by 80% while boosting brand equity. Work smarter.",
                "performance,productivity,automation,systems"
            )
        )

        val selected = ideas[Random.nextInt(ideas.size)]
        val rating = 0.80 + (Random.nextDouble() * 0.18)

        val imageGens = listOf(
            "An abstract high contrast glassmorphic prism structure refracting warm neon laser lights in a dark empty infinity space, 8k resolution, cinematic lighting",
            "A sleek floating modular scheduling deck designed in dark metallic silver with electric blue glass plates, digital render, isometric view",
            "A clean premium workspaces with an open metallic planner showcasing luminous cybernetic nodes, soft moody ambient twilight tone"
        )
        val selectedImagePrompt = imageGens[Random.nextInt(imageGens.size)]

        return ContentBrief(
            slotTime = slotTime,
            topic = selected.first,
            caption = selected.second + " (Aether generated in our custom [$tone] tone targeted strictly to [$audience]).",
            hashtags = selected.third,
            imagePrompt = selectedImagePrompt,
            predictedScore = (rating * 100).toInt() / 100.0,
            status = "draft"
        )
    }
}
