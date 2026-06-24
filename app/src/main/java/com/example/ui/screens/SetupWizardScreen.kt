package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AetherGlassCard
import com.example.ui.components.LiquidBackground
import com.example.ui.viewmodel.AetherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(viewModel: AetherViewModel) {
    val setupState by viewModel.setupState.collectAsState()
    val uriHandler = LocalUriHandler.current

    // Observe test states
    val supabaseTest by viewModel.supabaseTestResult.collectAsState()
    val facebookTest by viewModel.facebookTestResult.collectAsState()
    val mainAiTest by viewModel.mainAiTestResult.collectAsState()
    val imageAiTest by viewModel.imageAiTestResult.collectAsState()

    // Wizard Step state (1 to 5)
    var currentStep by remember { mutableStateOf(1) }

    // Inputs Step 1: Supabase
    var supabaseUrl by remember { mutableStateOf("https://your-project.supabase.co") }
    var anonKey by remember { mutableStateOf("") }
    var serviceRoleKey by remember { mutableStateOf("") }
    var patKey by remember { mutableStateOf("") }

    // Inputs Step 2: Facebook
    var facebookPageId by remember { mutableStateOf("") }
    var facebookToken by remember { mutableStateOf("") }

    // Inputs Step 3: Main AI
    var aiProvider by remember { mutableStateOf("openai_compatible") } // "gemini", "openai_compatible", "openrouter"
    var aiBaseUrl by remember { mutableStateOf("https://api.openai.com/v1") }
    var aiModel by remember { mutableStateOf("gpt-4o-mini") }
    var aiApiKey by remember { mutableStateOf("") }

    // Inputs Step 4: Image AI
    var imageProvider by remember { mutableStateOf("pollinations") } // "pollinations", "openai_compatible"
    var imageBaseUrl by remember { mutableStateOf("https://image.pollinations.ai") }
    var imageModel by remember { mutableStateOf("flux") }
    var imageApiKey by remember { mutableStateOf("") }

    // Inputs Step 5: Brand Voice & Frequency
    var projectName by remember { mutableStateOf("Aether Studio") }
    var brandTone by remember { mutableStateOf("Professional, Informative, Cyberpunk") }
    var brandTopics by remember { mutableStateOf("Tech Innovations, AI Design, Future Architectures") }
    var brandVoice by remember { mutableStateOf("Witty tech strategist with focus on design aesthetics") }
    var audience by remember { mutableStateOf("Indie Hackers, Product Designers, Creators") }
    var postFrequency by remember { mutableStateOf("2") }
    var postingMode by remember { mutableStateOf("hybrid") } // "manual", "hybrid", "full_auto"
    var dailySpendCap by remember { mutableStateOf("5.00") }

    var showSecrets by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    LiquidBackground {
        if (setupState.isRunning) {
            // Setup Progress Console
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Aether Provision Engine",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = setupState.statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LinearProgressIndicator(
                        progress = { setupState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "TERMINAL SYNCHRONIZATION LOG:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF020308) else Color(0xFF0F172A)) // Slate 900 dark theme log look in both modes
                            .border(1.dp, if (isDark) Color(0x1AFFFFFF) else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(setupState.logMessages) { log ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "> ",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFFEC4899),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = log,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = if (log.contains("⚠️") || log.contains("failed")) Color(0xFFFBBF24) else if (log.startsWith("❌")) Color(0xFFEF4444) else Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Form Display
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .safeDrawingPadding()
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                // Beautiful Header
                Text(
                    text = "ÆTHER",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "A U T O M A T I O N  W I Z A R D",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Navigation step bar indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Text(
                        text = "Step $currentStep of 5",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Step progress indicators dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (currentStep == i) 12.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (currentStep == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Steps UI
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                                slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                    },
                    label = "step_transition"
                ) { step ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (step) {
                            1 -> {
                                // STEP 1: Supabase Project Details
                                Text(
                                    text = "1. Supabase Datastore Backplane",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                AetherGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Text(
                                        text = "Setup the real distributed relational database for tracking calendar briefs, cost ledgers, and synchronized telemetry metadata.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }

                                WizardTextField(
                                    value = supabaseUrl,
                                    onValueChange = { supabaseUrl = it },
                                    label = "Supabase project URL",
                                    placeholder = "https://xxx.supabase.co",
                                    errorMessage = if (supabaseUrl.isNotEmpty() && !com.example.util.ValidationUtils.isValidUrl(supabaseUrl)) "Invalid URL format" else null
                                )

                                WizardTextField(
                                    value = anonKey,
                                    onValueChange = { anonKey = it },
                                    label = "Anon Key",
                                    placeholder = "eyJhbGciOi...",
                                    isSecret = true,
                                    showSecret = showSecrets,
                                    onToggleSecret = { showSecrets = !showSecrets },
                                    errorMessage = if (anonKey.isNotEmpty() && !com.example.util.ValidationUtils.isValidSupabaseKey(anonKey)) "Invalid Anon Key" else null
                                )

                                WizardTextField(
                                    value = serviceRoleKey,
                                    onValueChange = { serviceRoleKey = it },
                                    label = "Service Role Key",
                                    placeholder = "eyJhbG...",
                                    isSecret = true,
                                    showSecret = showSecrets,
                                    onToggleSecret = { showSecrets = !showSecrets },
                                    errorMessage = if (serviceRoleKey.isNotEmpty() && !com.example.util.ValidationUtils.isValidSupabaseKey(serviceRoleKey)) "Invalid Service Role Key" else null
                                )

                                WizardTextField(
                                    value = patKey,
                                    onValueChange = { patKey = it },
                                    label = "Personal Access Token",
                                    placeholder = "sbp_...",
                                    isSecret = true,
                                    showSecret = showSecrets,
                                    onToggleSecret = { showSecrets = !showSecrets },
                                    errorMessage = if (patKey.isNotEmpty() && patKey.length < 10) "Token is too short" else null
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                 // Show Active test diagnostics feedback
                                val t = supabaseTest
                                if (t != null) {
                                    val testColor = if (t.isSuccess) (if (isDark) Color(0xFF34D399) else Color(0xFF047857)) else (if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C))
                                    val testBg = if (t.isSuccess) (if (isDark) Color(0x1F10B981) else Color(0xFFECFDF5)) else (if (isDark) Color(0x1FEF4444) else Color(0xFFFEF2F2))
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = testBg)
                                    ) {
                                        Text(
                                            text = t.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = testColor,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                val step1Errors = listOf(
                                    supabaseUrl.isNotEmpty() && !com.example.util.ValidationUtils.isValidUrl(supabaseUrl),
                                    anonKey.isNotEmpty() && !com.example.util.ValidationUtils.isValidSupabaseKey(anonKey),
                                    serviceRoleKey.isNotEmpty() && !com.example.util.ValidationUtils.isValidSupabaseKey(serviceRoleKey),
                                    patKey.isNotEmpty() && patKey.length < 10
                                ).any { it }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.testSupabaseConnection(supabaseUrl, anonKey) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step1Errors) Color.Gray else MaterialTheme.colorScheme.secondary,
                                            contentColor = if (step1Errors) Color.LightGray else MaterialTheme.colorScheme.onSecondary
                                        ),
                                        enabled = !step1Errors
                                    ) {
                                        Text("Test", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveIncrementalSettings {
                                                it.copy(
                                                    supabaseUrl = supabaseUrl,
                                                    anonKey = anonKey,
                                                    serviceRoleKey = serviceRoleKey,
                                                    patKey = patKey
                                                )
                                            }
                                            currentStep = 2
                                        },
                                        modifier = Modifier.weight(1.5f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step1Errors) Color.Gray else MaterialTheme.colorScheme.primary,
                                            contentColor = if (step1Errors) Color.LightGray else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        enabled = !step1Errors
                                    ) {
                                        Text("Save & continue", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            2 -> {
                                // STEP 2: Facebook config
                                Text(
                                    text = "2. Facebook Page Integration",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                AetherGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Text(
                                        text = "Authorize direct, fully automated publishing of approved graphics & creative briefs onto your Facebook Page feed using Graph nodes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }

                                WizardTextField(
                                    value = facebookPageId,
                                    onValueChange = { facebookPageId = it },
                                    label = "Facebook Page ID",
                                    placeholder = "e.g. 10459746237...",
                                    errorMessage = if (facebookPageId.isNotEmpty() && !facebookPageId.all { char -> char.isDigit() }) "Page ID should contain only numbers" else null
                                )

                                WizardTextField(
                                    value = facebookToken,
                                    onValueChange = { facebookToken = it },
                                    label = "Facebook Page Access Key (Token)",
                                    placeholder = "EAAW...",
                                    isSecret = true,
                                    showSecret = showSecrets,
                                    onToggleSecret = { showSecrets = !showSecrets },
                                    errorMessage = if (facebookToken.isNotEmpty() && !com.example.util.ValidationUtils.isValidFacebookToken(facebookToken)) "Invalid Facebook Token" else null
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val t = facebookTest
                                if (t != null) {
                                    val testColor = if (t.isSuccess) (if (isDark) Color(0xFF34D399) else Color(0xFF047857)) else (if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C))
                                    val testBg = if (t.isSuccess) (if (isDark) Color(0x1F10B981) else Color(0xFFECFDF5)) else (if (isDark) Color(0x1FEF4444) else Color(0xFFFEF2F2))
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = testBg)
                                    ) {
                                        Text(
                                            text = t.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = testColor,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                val step2Errors = listOf(
                                    facebookPageId.isNotEmpty() && !facebookPageId.all { char -> char.isDigit() },
                                    facebookToken.isNotEmpty() && !com.example.util.ValidationUtils.isValidFacebookToken(facebookToken)
                                ).any { it }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.testFacebookConnection(facebookToken, facebookPageId) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step2Errors) Color.Gray else MaterialTheme.colorScheme.secondary,
                                            contentColor = if (step2Errors) Color.LightGray else MaterialTheme.colorScheme.onSecondary
                                        ),
                                        enabled = !step2Errors
                                    ) {
                                        Text("Test", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveIncrementalSettings {
                                                it.copy(
                                                    facebookPageId = facebookPageId,
                                                    facebookToken = facebookToken
                                                )
                                            }
                                            currentStep = 3
                                        },
                                        modifier = Modifier.weight(1.5f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step2Errors) Color.Gray else MaterialTheme.colorScheme.primary,
                                            contentColor = if (step2Errors) Color.LightGray else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        enabled = !step2Errors
                                    ) {
                                        Text("Save & continue", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            3 -> {
                                // STEP 3: Main AI config
                                Text(
                                    text = "3. Main Generative Brain",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                AetherGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Text(
                                        text = "Main language model processor used to draft creative briefs, brainstorm captions, pair hashtag clusters, and plan image compositions. Open AI Compatible & Gemini are supported.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }

                                // Custom selectable registry chips
                                Text("Provider Compatible Mode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("gemini" to "Google Gemini", "openai_compatible" to "OpenAI / Custom").forEach { (prov, label) ->
                                        val isSel = aiProvider == prov
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                WizardTextField(
                                    value = aiBaseUrl,
                                    onValueChange = { aiBaseUrl = it },
                                    label = "Base URL",
                                    placeholder = "e.g. https://api.openai.com/v1",
                                    errorMessage = if (aiBaseUrl.isNotEmpty() && !com.example.util.ValidationUtils.isValidUrl(aiBaseUrl)) "Invalid URL format" else null
                                )

                                WizardTextField(
                                    value = aiModel,
                                    onValueChange = { aiModel = it },
                                    label = "Model Name/ID",
                                    placeholder = "e.g. gpt-4o-mini or gemini-2.5-flash",
                                    errorMessage = if (aiModel.isBlank()) "Model cannot be empty" else null
                                )

                                WizardTextField(
                                    value = aiApiKey,
                                    onValueChange = { aiApiKey = it },
                                    label = "API Key",
                                    placeholder = "sk-xxx...",
                                    isSecret = true,
                                    showSecret = showSecrets,
                                    onToggleSecret = { showSecrets = !showSecrets },
                                    errorMessage = if (aiApiKey.isNotEmpty() && !com.example.util.ValidationUtils.isValidOpenAiKey(aiApiKey)) "Invalid AI API Key" else null
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val t = mainAiTest
                                if (t != null) {
                                    val testColor = if (t.isSuccess) (if (isDark) Color(0xFF34D399) else Color(0xFF047857)) else (if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C))
                                    val testBg = if (t.isSuccess) (if (isDark) Color(0x1F10B981) else Color(0xFFECFDF5)) else (if (isDark) Color(0x1FEF4444) else Color(0xFFFEF2F2))
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = testBg)
                                    ) {
                                        Text(
                                            text = t.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = testColor,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                val step3Errors = listOf(
                                    aiBaseUrl.isNotEmpty() && !com.example.util.ValidationUtils.isValidUrl(aiBaseUrl),
                                    aiModel.isBlank(),
                                    aiApiKey.isNotEmpty() && !com.example.util.ValidationUtils.isValidOpenAiKey(aiApiKey)
                                ).any { it }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.testMainAIConnection(aiProvider, aiBaseUrl, aiModel, aiApiKey) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step3Errors) Color.Gray else MaterialTheme.colorScheme.secondary,
                                            contentColor = if (step3Errors) Color.LightGray else MaterialTheme.colorScheme.onSecondary
                                        ),
                                        enabled = !step3Errors
                                    ) {
                                        Text("Test endpoint", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveIncrementalSettings {
                                                it.copy(
                                                    aiProvider = aiProvider,
                                                    aiBaseUrl = aiBaseUrl,
                                                    aiModel = aiModel,
                                                    aiApiKey = aiApiKey
                                                )
                                            }
                                            currentStep = 4
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step3Errors) Color.Gray else MaterialTheme.colorScheme.primary,
                                            contentColor = if (step3Errors) Color.LightGray else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        enabled = !step3Errors
                                    ) {
                                        Text("Save continue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            4 -> {
                                // STEP 4: Image AI
                                Text(
                                    text = "4. Creative Image AI Engine",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                AetherGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Text(
                                        text = "Configure the machine vision rendering pipeline. Select Pollinations AI (free, no apiKey needed, flux driven) or any custom OpenAI-compatible DALL-E endpoint.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }

                                Text("Image Provider Registry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("pollinations" to "Pollinations AI (Free)", "openai_compatible" to "OpenAI Images").forEach { (prov, label) ->
                                        val isSel = imageProvider == prov
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                WizardTextField(
                                    value = imageBaseUrl,
                                    onValueChange = { imageBaseUrl = it },
                                    label = "Base URL",
                                    placeholder = "e.g. https://image.pollinations.ai",
                                    errorMessage = if (imageBaseUrl.isNotEmpty() && !com.example.util.ValidationUtils.isValidUrl(imageBaseUrl)) "Invalid URL format" else null
                                )

                                WizardTextField(
                                    value = imageModel,
                                    onValueChange = { imageModel = it },
                                    label = "Model Name",
                                    placeholder = "e.g. flux, flux-realism, dall-e-3",
                                    errorMessage = if (imageModel.isBlank()) "Model cannot be empty" else null
                                )

                                WizardTextField(
                                    value = imageApiKey,
                                    onValueChange = { imageApiKey = it },
                                    label = "API Key",
                                    placeholder = "sk-xxx... (Not required for Pollinations)",
                                    isSecret = true,
                                    showSecret = showSecrets,
                                    onToggleSecret = { showSecrets = !showSecrets },
                                    errorMessage = if (imageApiKey.isNotEmpty() && imageProvider == "openai_compatible" && !com.example.util.ValidationUtils.isValidOpenAiKey(imageApiKey)) "Invalid API Key" else null
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val t = imageAiTest
                                if (t != null) {
                                    val testColor = if (t.isSuccess) (if (isDark) Color(0xFF34D399) else Color(0xFF047857)) else (if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C))
                                    val testBg = if (t.isSuccess) (if (isDark) Color(0x1F10B981) else Color(0xFFECFDF5)) else (if (isDark) Color(0x1FEF4444) else Color(0xFFFEF2F2))
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = testBg)
                                    ) {
                                        Text(
                                            text = t.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = testColor,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                val step4Errors = listOf(
                                    imageBaseUrl.isNotEmpty() && !com.example.util.ValidationUtils.isValidUrl(imageBaseUrl),
                                    imageModel.isBlank(),
                                    imageApiKey.isNotEmpty() && imageProvider == "openai_compatible" && !com.example.util.ValidationUtils.isValidOpenAiKey(imageApiKey)
                                ).any { it }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.testImageAIConnection(imageProvider, imageBaseUrl, imageModel, imageApiKey) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step4Errors) Color.Gray else MaterialTheme.colorScheme.secondary,
                                            contentColor = if (step4Errors) Color.LightGray else MaterialTheme.colorScheme.onSecondary
                                        ),
                                        enabled = !step4Errors
                                    ) {
                                        Text("Test endpoint", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveIncrementalSettings {
                                                it.copy(
                                                    imageProvider = imageProvider,
                                                    imageBaseUrl = imageBaseUrl,
                                                    imageModel = imageModel,
                                                    imageApiKey = imageApiKey
                                                )
                                            }
                                            currentStep = 5
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (step4Errors) Color.Gray else MaterialTheme.colorScheme.primary,
                                            contentColor = if (step4Errors) Color.LightGray else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        enabled = !step4Errors
                                    ) {
                                        Text("Save continue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            5 -> {
                                // STEP 5: Brand voice & Settings Setup
                                Text(
                                    text = "5. Brand Identity & Automations",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                WizardTextField(
                                    value = projectName,
                                    onValueChange = { projectName = it },
                                    label = "Brand / Project Name",
                                    placeholder = "e.g. Aether Space",
                                    errorMessage = if (projectName.isBlank()) "Project name cannot be empty" else null
                                )

                                WizardTextField(
                                    value = brandTone,
                                    onValueChange = { brandTone = it },
                                    label = "Tone Description",
                                    placeholder = "e.g. Professional, energetic"
                                )

                                WizardTextField(
                                    value = brandTopics,
                                    onValueChange = { brandTopics = it },
                                    label = "Topics (comma separated)",
                                    placeholder = "Tech, SaaS Workflow, Cyber Aesthetics"
                                )

                                WizardTextField(
                                    value = brandVoice,
                                    onValueChange = { brandVoice = it },
                                    label = "Comprehensive Brand Blueprint",
                                    placeholder = "An optimistic developer building scalable glass nodes"
                                )

                                WizardTextField(
                                    value = audience,
                                    onValueChange = { audience = it },
                                    label = "Target Audience",
                                    placeholder = "M3 builders, founders"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Post Frequency / Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                        OutlinedTextField(
                                            value = postFrequency,
                                            onValueChange = { postFrequency = it },
                                            singleLine = true,
                                            maxLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Spend Cap / Day ($)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                        OutlinedTextField(
                                            value = dailySpendCap,
                                            onValueChange = { dailySpendCap = it },
                                            singleLine = true,
                                            maxLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                Text("Automation Posting Mode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("manual" to "Manual", "hybrid" to "Hybrid", "full_auto" to "Full Auto").forEach { (mode, label) ->
                                        val isSel = postingMode == mode
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                val step5Errors = listOf(
                                    projectName.isBlank(),
                                    postFrequency.toIntOrNull() == null || (postFrequency.toIntOrNull() ?: 0) < 1,
                                    dailySpendCap.toDoubleOrNull() == null || (dailySpendCap.toDoubleOrNull() ?: 0.0) < 0.0
                                ).any { it }

                                Button(
                                    onClick = {
                                        viewModel.executeAutoSetup(
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
                                            maxPostsPerDay = postFrequency.toIntOrNull() ?: 2,
                                            dailySpendCap = dailySpendCap.toDoubleOrNull() ?: 5.0
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (step5Errors) Color.Gray else MaterialTheme.colorScheme.primary,
                                        contentColor = if (step5Errors) Color.LightGray else MaterialTheme.colorScheme.onPrimary
                                    ),
                                    enabled = !step5Errors
                                ) {
                                    Text(
                                        text = "RUN SETUP",
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isSecret: Boolean = false,
    showSecret: Boolean = false,
    onToggleSecret: (() -> Unit)? = null,
    errorMessage: String? = null
) {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            singleLine = true,
            isError = errorMessage != null,
            placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)) },
            visualTransformation = if (isSecret && !showSecret) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isSecret && onToggleSecret != null) {
                    IconButton(onClick = onToggleSecret) {
                        Icon(
                            imageVector = if (showSecret) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Secret",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
