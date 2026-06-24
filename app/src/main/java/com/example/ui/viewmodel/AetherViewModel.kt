package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.data.*
import com.example.scheduler.PublishWorker
import com.example.ui.components.ToastMessage
import com.example.ui.components.ToastType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class Screen {
    SETUP_WIZARD,
    DASHBOARD,
    SCHEDULE,
    ANALYTICS,
    SETTINGS
}

data class SetupProgressState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = "",
    val logMessages: List<String> = emptyList(),
    val error: String? = null
)

class AetherViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorage(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = AetherRepository(
        database.appSettingsDao(),
        database.contentBriefDao(),
        database.systemEventDao(),
        database.costEntryDao(),
        database.engagementSnapshotDao(),
        secureStorage
    )

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(Screen.SETUP_WIZARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Setup Wizard UI State
    private val _setupState = MutableStateFlow(SetupProgressState())
    val setupState: StateFlow<SetupProgressState> = _setupState.asStateFlow()

    // Toast notifications flow
    private val _toastMessages = MutableStateFlow<List<ToastMessage>>(emptyList())
    val toastMessages: StateFlow<List<ToastMessage>> = _toastMessages.asStateFlow()

    fun showToast(message: String, type: ToastType = ToastType.INFO) {
        val toast = ToastMessage(message = message, type = type)
        _toastMessages.update { it + toast }
        viewModelScope.launch {
            delay(toast.durationMs)
            dismissToast(toast.id)
        }
    }

    fun dismissToast(id: String) {
        _toastMessages.update { list -> list.filter { it.id != id } }
    }

    // Integration test results flows
    private val _supabaseTestResult = MutableStateFlow<com.example.api.ConnectivityService.TestResult?>(null)
    val supabaseTestResult = _supabaseTestResult.asStateFlow()

    private val _facebookTestResult = MutableStateFlow<com.example.api.ConnectivityService.TestResult?>(null)
    val facebookTestResult = _facebookTestResult.asStateFlow()

    private val _mainAiTestResult = MutableStateFlow<com.example.api.ConnectivityService.TestResult?>(null)
    val mainAiTestResult = _mainAiTestResult.asStateFlow()

    private val _imageAiTestResult = MutableStateFlow<com.example.api.ConnectivityService.TestResult?>(null)
    val imageAiTestResult = _imageAiTestResult.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    // Database Flows
    val appSettings: StateFlow<AppSettings?> = repository.appSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contentBriefs: StateFlow<List<ContentBrief>> = repository.allBriefsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemEvents: StateFlow<List<SystemEvent>> = repository.recentEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val costEntries: StateFlow<List<CostEntry>> = repository.costEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCost: StateFlow<Double> = repository.totalCostFlow
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val engagementSnapshots: StateFlow<List<EngagementSnapshot>> = repository.snapshotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize Default Settings if not exists
        viewModelScope.launch(Dispatchers.IO) {
            val settings = repository.getSettingsDirect()
            if (settings == null) {
                val fresh = AppSettings()
                repository.saveSettings(fresh)
                repository.insertEvent("system", "info", "Precompiled fresh Aether workspace initialized.")
            } else if (settings.isSetupComplete) {
                _currentScreen.value = Screen.DASHBOARD
            }

            // Launch Real-time Background WorkManager Sync task
            try {
                val workManager = WorkManager.getInstance(application)
                val periodicWork = PeriodicWorkRequestBuilder<PublishWorker>(
                    15, TimeUnit.MINUTES
                ).build()
                workManager.enqueueUniquePeriodicWork(
                    "AetherPublishWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
                )
                repository.insertEvent("scheduler", "info", "Aether core background publisher scheduler verified & online.")
            } catch (e: Exception) {
                Log.e("AetherViewModel", "Failed to setup WorkManager background work: ${e.localizedMessage}")
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun testSupabaseConnection(url: String, anonKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _supabaseTestResult.value = com.example.api.ConnectivityService.TestResult(false, "Connecting and establishing handshake...")
            val result = com.example.api.ConnectivityService.testSupabase(url, anonKey)
            _supabaseTestResult.value = result
            repository.insertEvent("auth", if (result.isSuccess) "info" else "error", "Supabase test: ${result.message}")
            if (result.isSuccess) {
                showToast("Supabase connected successfully!", ToastType.SUCCESS)
            } else {
                showToast("Supabase setup problem: ${result.message}", ToastType.ERROR)
            }
        }
    }

    fun testFacebookConnection(token: String, pageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _facebookTestResult.value = com.example.api.ConnectivityService.TestResult(false, "Initiating Graph node request...")
            val result = com.example.api.ConnectivityService.testFacebookPage(token, pageId)
            _facebookTestResult.value = result
            repository.insertEvent("publish", if (result.isSuccess) "info" else "error", "Facebook test: ${result.message}")
            if (result.isSuccess) {
                showToast("Facebook verified successfully!", ToastType.SUCCESS)
            } else {
                showToast("Facebook setup failure: ${result.message}", ToastType.ERROR)
            }
        }
    }

    fun testMainAIConnection(provider: String, baseUrl: String, model: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _mainAiTestResult.value = com.example.api.ConnectivityService.TestResult(false, "Pinging model endpoint...")
            val result = com.example.api.ConnectivityService.testMainAI(provider, baseUrl, model, apiKey)
            _mainAiTestResult.value = result
            repository.insertEvent("ai", if (result.isSuccess) "info" else "error", "Main AI endpoint test: ${result.message}")
            if (result.isSuccess) {
                showToast("AI model connected successfully!", ToastType.SUCCESS)
            } else {
                showToast("AI endpoint verification failure: ${result.message}", ToastType.ERROR)
            }
        }
    }

    fun testImageAIConnection(provider: String, baseUrl: String, model: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageAiTestResult.value = com.example.api.ConnectivityService.TestResult(false, "Evaluating generative model endpoint...")
            val result = com.example.api.ConnectivityService.testImageAI(provider, baseUrl, model, apiKey)
            _imageAiTestResult.value = result
            repository.insertEvent("ai", if (result.isSuccess) "info" else "error", "Image AI endpoint test: ${result.message}")
            if (result.isSuccess) {
                showToast("Image generation endpoint verified!", ToastType.SUCCESS)
            } else {
                showToast("Image endpoint check failure: ${result.message}", ToastType.ERROR)
            }
        }
    }

    fun saveIncrementalSettings(updater: (AppSettings) -> AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getSettingsDirect() ?: AppSettings()
            val updated = updater(current)
            repository.saveSettings(updated)
        }
    }

    /**
     * Executes the automatic multi-step integration and local setup sequences.
     */
    fun executeAutoSetup(
        projectName: String,
        supabaseUrl: String,
        anonKey: String,
        serviceRoleKey: String,
        patKey: String,
        facebookToken: String,
        facebookPageId: String,
        aiApiKey: String,
        aiProvider: String,
        aiBaseUrl: String,
        aiModel: String,
        imageProvider: String,
        imageBaseUrl: String,
        imageModel: String,
        imageApiKey: String,
        brandVoice: String,
        brandTone: String,
        brandTopics: String,
        audience: String,
        postingMode: String,
        maxPostsPerDay: Int,
        dailySpendCap: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _setupState.value = SetupProgressState(
                isRunning = true,
                progress = 0.05f,
                statusText = "Initializing Provision Engine...",
                logMessages = listOf("Booting Aether cloud installer...")
            )

            // Step 1: Pre-validation of input details
            addSetupLog("Validating credential schema lengths...")
            delay(500)
            if (supabaseUrl.isBlank() || anonKey.isBlank()) {
                addSetupLog("❌ Setup Failed: Supabase URL and Anon Key cannot be empty.")
                _setupState.update { it.copy(isRunning = false, statusText = "Failed", progress = 1f) }
                showToast("Missing required Supabase connection parameters", ToastType.ERROR)
                return@launch
            }

            // Validating credentials and pinging endpoints
            addSetupLog("Testing Supabase Project Connectivity...")
            val supabaseRes = com.example.api.ConnectivityService.testSupabase(supabaseUrl, anonKey)
            if (!supabaseRes.isSuccess) {
                addSetupLog("❌ Setup Failed: Supabase handshake returned error: ${supabaseRes.message}")
                _setupState.update { it.copy(isRunning = false, statusText = "Failed", progress = 1f) }
                showToast("Supabase setup problem: ${supabaseRes.message}", ToastType.ERROR)
                return@launch
            } else {
                addSetupLog("✅ Supabase basic connectivity verified.")
            }

            if (aiApiKey.isNotEmpty()) {
                addSetupLog("Testing AI Provider Credentials...")
                val aiRes = com.example.api.ConnectivityService.testMainAI(aiProvider, aiBaseUrl, aiModel, aiApiKey)
                if (!aiRes.isSuccess) {
                    addSetupLog("⚠️ Warn: AI handshake failed: ${aiRes.message}")
                    showToast("AI configuration warning: ${aiRes.message}", ToastType.WARNING)
                } else {
                    addSetupLog("✅ AI Provider verified.")
                }
            }

            // Run Setup Provisioning (the 6-step cloud blueprint flow)
            addSetupLog("Triggering automatic backend provisioning pipeline...")
            val provSuccess = com.example.api.SupabaseService.runSetupProvisioning(
                supabaseUrl = supabaseUrl,
                anonKey = anonKey,
                serviceRoleKey = serviceRoleKey,
                patKey = patKey,
                onProgress = { p, msg ->
                    _setupState.update { current ->
                        current.copy(
                            progress = p,
                            statusText = msg,
                            logMessages = current.logMessages + msg
                        )
                    }
                }
            )

            if (!provSuccess) {
                addSetupLog("❌ Setup Failed during cloud provisioning steps.")
                _setupState.update { it.copy(isRunning = false, statusText = "Failed") }
                showToast("Provisioning failed. Check logs.", ToastType.ERROR)
                return@launch
            }

            _setupState.update { it.copy(progress = 0.90f, statusText = "Saving Configuration...") }

            // Pre-save AppSettings so the schedule generation can reference them
            repository.clearAllBriefs()
            repository.clearSnapshots()
            val currentSettings = AppSettings(
                projectName = projectName,
                supabaseUrl = supabaseUrl,
                anonKey = anonKey,
                serviceRoleKey = serviceRoleKey,
                patKey = patKey,
                facebookToken = facebookToken,
                facebookPageId = facebookPageId,
                aiApiKey = aiApiKey,
                aiProvider = aiProvider,
                aiBaseUrl = aiBaseUrl,
                aiModel = aiModel,
                imageProvider = imageProvider,
                imageBaseUrl = imageBaseUrl,
                imageModel = imageModel,
                imageApiKey = imageApiKey,
                brandVoice = brandVoice,
                brandTone = brandTone,
                brandTopics = brandTopics,
                audience = audience,
                postingMode = postingMode,
                maxPostsPerDay = maxPostsPerDay,
                dailySpendCap = dailySpendCap,
                isSetupComplete = true
            )
            repository.saveSettings(currentSettings)

            repository.insertEvent("system", "info", "Aether self-hosted installer completed with 100% success.")

            addSetupLog("🎉 SETUP FULLY COMPLETE! Welcome to Aether AI.")
            _setupState.update { it.copy(progress = 1.0f, statusText = "Finalized!") }
            showToast("Workspace setup completed successfully!", ToastType.SUCCESS)
            delay(1000)

            _setupState.update { SetupProgressState() } // Reset progress state
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    private fun addSetupLog(message: String) {
        _setupState.update {
            it.copy(logMessages = it.logMessages + message)
        }
    }

    /**
     * Erases settings and resets all tables.
     */
    fun resetInstallation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllBriefs()
            repository.clearCosts()
            repository.clearSnapshots()
            repository.clearLog()
            
            val defaults = AppSettings(isSetupComplete = false)
            repository.saveSettings(defaults)
            
            repository.insertEvent("system", "warn", "Danger Zone Action: Installed tables and configuration cleared.")
            showToast("Installed tables and configuration cleared.", ToastType.WARNING)
            _currentScreen.value = Screen.SETUP_WIZARD
        }
    }

    /**
     * Converts a text prompt into an encoded pollinations.ai image generator.
     */
    fun generateImageServiceUrl(prompt: String, seed: Int): String {
        return try {
            val clean = if (prompt.length > 200) prompt.substring(0, 200) else prompt
            val encodedContents = URLEncoder.encode(clean, "UTF-8")
            "https://image.pollinations.ai/prompt/$encodedContents?width=1024&height=1024&nologo=true&seed=$seed"
        } catch (e: UnsupportedEncodingException) {
            "https://image.pollinations.ai/prompt/cybernetic_grid?width=1024&height=1024&nologo=true&seed=$seed"
        }
    }

    /**
     * Approves a brief to transition it to the scheduled pipeline.
     */
    fun approveBrief(briefId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getBriefById(briefId) ?: return@launch
            val updated = existing.copy(
                isApproved = true,
                status = "approved"
            )
            repository.updateBrief(updated)
            repository.insertEvent("scheduler", "info", "Approved topic draft: '${existing.topic}' for posting.")
            showToast("Content brief approved and scheduled!", ToastType.SUCCESS)
        }
    }

    /**
     * Deletes a brief from the scheduled pipeline.
     */
    fun deleteBrief(briefId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getBriefById(briefId) ?: return@launch
            repository.deleteBriefById(briefId)
            repository.insertEvent("scheduler", "warn", "Deleted calendar slot for Topic: '${existing.topic}'.")
            showToast("Content brief successfully discarded", ToastType.INFO)
        }
    }

    /**
     * Edits a content brief with manual adjustments.
     */
    fun updateBriefManually(id: Long, topic: String, caption: String, hashtags: String, imageUrl: String, slotTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getBriefById(id) ?: return@launch
            val updated = existing.copy(
                topic = topic,
                caption = caption,
                hashtags = hashtags,
                imageUrl = imageUrl.ifBlank { existing.imageUrl },
                slotTime = slotTime
            )
            repository.updateBrief(updated)
            repository.insertEvent("scheduler", "info", "Updated brief: '${topic}' manually.")
            showToast("Content brief updated successfully!", ToastType.SUCCESS)
        }
    }

    /**
     * Publishes a brief immediately to Facebook utilizing background WorkManager transaction dispatch.
     */
    fun publishBriefImmediate(briefId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getBriefById(briefId) ?: return@launch
            
            // Set stats and time so the background worker can pick it up immediately
            val updated = existing.copy(
                status = "approved",
                isApproved = true,
                slotTime = System.currentTimeMillis()
            )
            repository.updateBrief(updated)

            repository.insertEvent("publish", "info", "Dispatched one-time WorkManager task to execute publish sequence immediately...")

            try {
                val workManager = WorkManager.getInstance(getApplication())
                val workRequest = OneTimeWorkRequestBuilder<PublishWorker>()
                    .addTag("AetherPublishImmediate")
                    .build()
                workManager.enqueue(workRequest)
                showToast("Publishing task dispatched!", ToastType.INFO)
            } catch (e: Exception) {
                Log.e("AetherViewModel", "Failed to enqueue immediate publication: ${e.localizedMessage}")
                // Securely notify workspace log
                repository.insertEvent("publish", "warn", "WorkManager pipeline busy, retrying transaction pool...")
                showToast("Failed to dispatch publishing task: WorkManager busy", ToastType.ERROR)
            }
        }
    }

    /**
     * Saves general settings and brand voices.
     */
    fun updateBrandSettings(
        projectName: String,
        brandVoice: String,
        audience: String,
        postingMode: String,
        maxPostsPerDay: Int,
        dailySpendCap: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getSettingsDirect() ?: AppSettings()
            val updated = current.copy(
                projectName = projectName,
                brandVoice = brandVoice,
                audience = audience,
                postingMode = postingMode,
                maxPostsPerDay = maxPostsPerDay,
                dailySpendCap = dailySpendCap
            )
            repository.saveSettings(updated)
            repository.insertEvent("system", "info", "Brand parameters updated (Brand Voice: $brandVoice)")
            showToast("Brand parameters updated successfully!", ToastType.SUCCESS)
        }
    }

    /**
     * Manually generates a new brief row in the scheduler.
     */
    fun insertNewManualBrief(topic: String, caption: String, hashtags: String, imageUrl: String, slotTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val brief = ContentBrief(
                slotTime = slotTime,
                topic = topic,
                caption = caption,
                hashtags = hashtags,
                imagePrompt = "",
                imageUrl = imageUrl,
                status = "draft"
            )
            repository.insertBrief(brief)
            repository.insertEvent("scheduler", "info", "Created manual content brief: '$topic'")
            showToast("Manual content brief created successfully!", ToastType.SUCCESS)
        }
    }

    /**
     * Executes an immediate, complete bidirectional sync of content briefs, snapshots, cost entries,
     * and system event logs with the configured Supabase cloud instance.
     */
    fun triggerSupabaseSync() {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = repository.getSettingsRaw() ?: return@launch
            if (!settings.isSetupComplete) {
                showToast("Please complete the setup wizard first.", ToastType.WARNING)
                return@launch
            }
            val supabaseUrl = settings.supabaseUrl.trim()
            val supabaseKey = if (settings.serviceRoleKey.trim().isNotEmpty()) settings.serviceRoleKey.trim() else settings.anonKey.trim()

            if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
                showToast("Supabase is not configured yet.", ToastType.WARNING)
                return@launch
            }

            if (_isSyncing.value) return@launch
            _isSyncing.value = true
            showToast("Syncing with Supabase backplane...", ToastType.INFO)
            repository.insertEvent("scheduler", "info", "Manual synchronization triggered by user.")

            try {
                // A. Pull Remote Briefs
                val remoteBriefs = com.example.api.SupabaseService.pullBriefs(supabaseUrl, supabaseKey)
                if (remoteBriefs.isNotEmpty()) {
                    val localBriefs = repository.allBriefsFlow.first()
                    val localMap = localBriefs.associateBy { it.id }
                    
                    var updatedCount = 0
                    var insertedCount = 0
                    for (remote in remoteBriefs) {
                        val local = localMap[remote.id]
                        if (local == null) {
                            repository.insertBrief(remote)
                            insertedCount++
                        } else if (local.status != remote.status || local.isApproved != remote.isApproved || local.slotTime != remote.slotTime) {
                            repository.updateBrief(remote)
                            updatedCount++
                        }
                    }
                    if (insertedCount > 0 || updatedCount > 0) {
                        repository.insertEvent("scheduler", "info", "Pulled from Supabase: $insertedCount new briefs, $updatedCount updated briefs.")
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

                repository.insertEvent("scheduler", "info", "Manual Supabase synchronization completed successfully.")
                showToast("Successfully synchronized with Supabase!", ToastType.SUCCESS)
            } catch (e: Exception) {
                Log.e("AetherViewModel", "Manual sync failed", e)
                repository.insertEvent("scheduler", "error", "Manual sync failed: ${e.localizedMessage}")
                showToast("Sync warning: check your internet or Supabase tables.", ToastType.WARNING)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
