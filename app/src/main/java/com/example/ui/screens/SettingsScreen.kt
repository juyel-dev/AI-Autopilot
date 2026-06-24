package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.ui.components.AetherGlassCard
import com.example.ui.components.LiquidBackground
import com.example.ui.viewmodel.AetherViewModel

@Composable
fun SettingsScreen(viewModel: AetherViewModel) {
    val settings by viewModel.appSettings.collectAsState()

    val currentSettings = settings ?: AppSettings()

    // Configuration states
    var projectName by remember { mutableStateOf(currentSettings.projectName) }
    var brandVoice by remember { mutableStateOf(currentSettings.brandVoice) }
    var audience by remember { mutableStateOf(currentSettings.audience) }
    var postingMode by remember { mutableStateOf(currentSettings.postingMode) }
    var maxPostsPerDay by remember { mutableStateOf(currentSettings.maxPostsPerDay.toString()) }
    var dailySpendCap by remember { mutableStateOf(currentSettings.dailySpendCap.toString()) }

    var supabaseUrl by remember { mutableStateOf(currentSettings.supabaseUrl) }
    var anonKey by remember { mutableStateOf(currentSettings.anonKey) }
    var serviceRoleKey by remember { mutableStateOf(currentSettings.serviceRoleKey) }
    var patKey by remember { mutableStateOf(currentSettings.patKey) }
    var facebookToken by remember { mutableStateOf(currentSettings.facebookToken) }
    var aiApiKey by remember { mutableStateOf(currentSettings.aiApiKey) }
    var aiModel by remember { mutableStateOf(currentSettings.aiModel) }

    var showSecretsBlock by remember { mutableStateOf(false) }
    var showPasswords by remember { mutableStateOf(false) }
    var showResetVerify by remember { mutableStateOf(false) }

    // Sync settings if they change remotely
    LaunchedEffect(settings) {
        settings?.let {
            projectName = it.projectName
            brandVoice = it.brandVoice
            audience = it.audience
            postingMode = it.postingMode
            maxPostsPerDay = it.maxPostsPerDay.toString()
            dailySpendCap = it.dailySpendCap.toString()
            supabaseUrl = it.supabaseUrl
            anonKey = it.anonKey
            serviceRoleKey = it.serviceRoleKey
            patKey = it.patKey
            facebookToken = it.facebookToken
            aiApiKey = it.aiApiKey
            aiModel = it.aiModel
        }
    }

    LiquidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            Text(
                text = "Workspace Configurations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Calibrate secret pathways and brand indices",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Block 1: Brand Parameters
            Text(
                text = "BRAND VOICE BLUEPRINT",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AetherGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                WizardTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = "Project Label",
                    placeholder = "Aether Hub"
                )

                WizardTextField(
                    value = brandVoice,
                    onValueChange = { brandVoice = it },
                    label = "Core Tone Strategy",
                    placeholder = "Describe tone e.g., corporate pro, cyber humor"
                )

                WizardTextField(
                    value = audience,
                    onValueChange = { audience = it },
                    label = "Target demographic",
                    placeholder = "Indie Founders, Developers"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        WizardTextField(
                            value = maxPostsPerDay,
                            onValueChange = { maxPostsPerDay = it },
                            label = "Max Posts/Day",
                            placeholder = "2"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        WizardTextField(
                            value = dailySpendCap,
                            onValueChange = { dailySpendCap = it },
                            label = "Spend Limit ($)",
                            placeholder = "5.0"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.updateBrandSettings(
                            projectName = projectName,
                            brandVoice = brandVoice,
                            audience = audience,
                            postingMode = postingMode,
                            maxPostsPerDay = maxPostsPerDay.toIntOrNull() ?: 2,
                            dailySpendCap = dailySpendCap.toDoubleOrNull() ?: 5.0
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Save Brand Settings")
                }
            }

            // Block 2: Key Integration Gateways
            Text(
                text = "SYNAPSE & ROUTER SECRETS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AetherGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Synapse Connections",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Switch(
                        checked = showSecretsBlock,
                        onCheckedChange = { showSecretsBlock = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF3B82F6),
                            checkedTrackColor = Color(0xFF3B82F6).copy(alpha = 0.3f)
                        )
                    )
                }

                if (showSecretsBlock) {
                    Spacer(modifier = Modifier.height(12.dp))

                    WizardTextField(
                        value = supabaseUrl,
                        onValueChange = { supabaseUrl = it },
                        label = "Supabase API Endpoint",
                        placeholder = "https://your-ref.supabase.co"
                    )

                    WizardTextField(
                        value = anonKey,
                        onValueChange = { anonKey = it },
                        label = "Supabase Anon Key",
                        placeholder = "eyJhbG...",
                        isSecret = true,
                        showSecret = showPasswords,
                        onToggleSecret = { showPasswords = !showPasswords }
                    )

                    WizardTextField(
                        value = serviceRoleKey,
                        onValueChange = { serviceRoleKey = it },
                        label = "Supabase Service Role Bypass",
                        placeholder = "eyJhbG...",
                        isSecret = true,
                        showSecret = showPasswords,
                        onToggleSecret = { showPasswords = !showPasswords }
                    )

                    WizardTextField(
                        value = patKey,
                        onValueChange = { patKey = it },
                        label = "Personal Access Token",
                        placeholder = "sbp_...",
                        isSecret = true,
                        showSecret = showPasswords,
                        onToggleSecret = { showPasswords = !showPasswords }
                    )

                    WizardTextField(
                        value = facebookToken,
                        onValueChange = { facebookToken = it },
                        label = "Facebook Access Token",
                        placeholder = "EAAW...",
                        isSecret = true,
                        showSecret = showPasswords,
                        onToggleSecret = { showPasswords = !showPasswords }
                    )

                    WizardTextField(
                        value = aiApiKey,
                        onValueChange = { aiApiKey = it },
                        label = "AI Generative Key (Gemini API / Open AI)",
                        placeholder = "AI API Key",
                        isSecret = true,
                        showSecret = showPasswords,
                        onToggleSecret = { showPasswords = !showPasswords }
                    )

                    WizardTextField(
                        value = aiModel,
                        onValueChange = { aiModel = it },
                        label = "Model Registry Identifier",
                        placeholder = "gemini-3.5-flash"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            // Custom rotate / save key function triggered directly inside model mapping
                            viewModel.saveIncrementalSettings {
                                it.copy(
                                    supabaseUrl = supabaseUrl,
                                    anonKey = anonKey,
                                    serviceRoleKey = serviceRoleKey,
                                    patKey = patKey,
                                    facebookToken = facebookToken,
                                    aiApiKey = aiApiKey,
                                    aiModel = aiModel
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Save Secrets")
                    }
                }
            }

            // Block 3: Danger Zone Wipes
            Text(
                text = "DANGER PIPELINE CONTROL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AetherGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Text(
                    text = "A full reset unlinks your Supabase server instances, drops local indexes, and wipes scheduled posts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (showResetVerify) {
                    Text(
                        text = "Are you absolutely sure? This operation is irreversible.",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showResetVerify = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Abort")
                        }
                        Button(
                            onClick = {
                                viewModel.resetInstallation()
                                showResetVerify = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Reset")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showResetVerify = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Wipe Workspace and Backplane")
                    }
                }
            }
        }
    }
}
