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
        val sanitizedApiKey = apiKey.trim()
        val realModel = if (modelName.isBlank()) {
            if (provider == "gemini") "gemini-3.5-flash" else "gpt-4o-mini"
        } else modelName

        if (sanitizedApiKey.isEmpty() || sanitizedApiKey == "MY_GEMINI_API_KEY" || sanitizedApiKey.startsWith("sk-demo")) {
            // Roll back to an incredibly polished template engine if no actual key is configured
            Log.w(TAG, "No valid premium API key configured. Falling back to local template compiler.")
            val brief = generateLocalBrief(pageTopic, brandVoice, audience, slotTime)
            repository.insertEvent(
                category = "ai",
                severity = "warn",
                message = "Generated slot local draft due to empty or mock API Key configuration."
            )
            return@withContext brief
        }

        val systemPrompt = """
            You are Aether AI, a professional social media content strategist. 
            Generate exactly 1 high-engagement Facebook post brief.
            Your output MUST be a valid JSON object only with exactly the following structure:
            {
              "topic": "Brief topic title",
              "caption": "Highly engaging caption in the requested brand voice. Do not include hashtags here.",
              "hashtags": "comma,separated,hashtags,without,hash,symbols",
              "imagePrompt": "A highly descriptive photography or digital art prompt for an image generator describing a gorgeous context-matching matching scene.",
              "predictedScore": 0.85
            }
            Do not include any markdown formatting like ```json or trailing text. Just the raw JSON.
        """.trimIndent()

        val userPrompt = """
            Generate content brief for:
            Page Niche/Topic: $pageTopic
            Brand Voice Tone: $brandVoice
            Target Audience: $audience
        """.trimIndent()

        try {
            if (provider == "gemini") {
                // Direct Gemini API
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", userPrompt) })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.75)
                    })
                }

                val requestUrl = "$GEMINI_BASE_URL$realModel:generateContent?key=$sanitizedApiKey"
                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(requestUrl).post(requestBody).build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errMessage = "AI API network error active: HTTP ${response.code}. Response: $responseStr"
                    Log.e(TAG, errMessage)
                    repository.insertEvent("ai", "error", "Gemini call failed: HTTP ${response.code}")
                    return@withContext generateLocalBrief(pageTopic, brandVoice, audience, slotTime)
                }

                val parsedJson = JSONObject(responseStr)
                val candidates = parsedJson.optJSONArray("candidates")
                val contentObj = candidates?.optJSONObject(0)?.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

                // Track usage
                val usageMetadata = parsedJson.optJSONObject("usageMetadata")
                val inputTokens = usageMetadata?.optInt("promptTokenCount") ?: 300
                val outputTokens = usageMetadata?.optInt("candidatesTokenCount") ?: 250
                val estimatedCost = (inputTokens * 0.00000015) + (outputTokens * 0.0000006)
                
                repository.insertCost("Gemini ($realModel)", realModel, inputTokens, outputTokens, estimatedCost)
                repository.insertEvent("ai", "info", "Brief successfully loaded via Gemini ($realModel). Cost: ${"%.5f".format(estimatedCost)}$")

                val cleanJsonText = cleanJsonResponse(rawText)
                val briefObj = JSONObject(cleanJsonText)

                return@withContext ContentBrief(
                    slotTime = slotTime,
                    topic = briefObj.optString("topic", "Innovative " + pageTopic.split(" ").firstOrNull().orEmpty()),
                    caption = briefObj.optString("caption", "Looking to shift your perspective on $pageTopic? Read on!"),
                    hashtags = briefObj.optString("hashtags", "automation,tech,design"),
                    imagePrompt = briefObj.optString("imagePrompt", "An abstract neon glassmorphism representation of cybernetical thoughts"),
                    predictedScore = briefObj.optDouble("predictedScore", 0.85),
                    status = "draft"
                )
            } else {
                // OpenAI Compatible Chat Completions API
                val cleanBase = baseUrl.trim().removeSuffix("/")
                val requestUrl = if (cleanBase.isEmpty()) "https://api.openai.com/v1/chat/completions" else "$cleanBase/chat/completions"

                val jsonBody = JSONObject().apply {
                    put("model", realModel)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", userPrompt)
                        })
                    })
                    put("temperature", 0.7)
                    // Request structured JSON object if endpoint supports it
                    put("response_format", JSONObject().apply { put("type", "json_object") })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .header("Authorization", "Bearer $sanitizedApiKey")
                    .header("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""
                
                if (!response.isSuccessful) {
                    val errMessage = "OpenAI compatible AI API failure: HTTP ${response.code}. Payload: $responseStr"
                    Log.e(TAG, errMessage)
                    repository.insertEvent("ai", "error", "OpenAI call failed: HTTP ${response.code}")
                    return@withContext generateLocalBrief(pageTopic, brandVoice, audience, slotTime)
                }

                val json = JSONObject(responseStr)
                val choices = json.getJSONArray("choices")
                val text = choices.getJSONObject(0).getJSONObject("message").getString("content")

                // Track cost (approximate tokens)
                val usage = json.optJSONObject("usage")
                val inputTokens = usage?.optInt("prompt_tokens") ?: 400
                val outputTokens = usage?.optInt("completion_tokens") ?: 300
                // Standard gpt-4o-mini pricing
                val estimatedCost = (inputTokens * 0.00000015) + (outputTokens * 0.0000006)

                repository.insertCost("OpenAI ($realModel)", realModel, inputTokens, outputTokens, estimatedCost)
                repository.insertEvent("ai", "info", "Brief successfully loaded via custom endpoint. Cost: ${"%.5f".format(estimatedCost)}$")

                val cleanJsonText = cleanJsonResponse(text)
                val briefObj = JSONObject(cleanJsonText)

                return@withContext ContentBrief(
                    slotTime = slotTime,
                    topic = briefObj.optString("topic", "Innovative " + pageTopic.split(" ").firstOrNull().orEmpty()),
                    caption = briefObj.optString("caption", "Looking to shift your perspective on $pageTopic? Read on!"),
                    hashtags = briefObj.optString("hashtags", "automation,tech,design"),
                    imagePrompt = briefObj.optString("imagePrompt", "An abstract neon glassmorphism representation of cybernetical thoughts"),
                    predictedScore = briefObj.optDouble("predictedScore", 0.85),
                    status = "draft"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during brief generation", e)
            repository.insertEvent("ai", "error", "Generative brief exception: ${e.localizedMessage}")
            return@withContext generateLocalBrief(pageTopic, brandVoice, audience, slotTime)
        }
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
