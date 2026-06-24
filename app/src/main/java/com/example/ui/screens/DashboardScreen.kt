package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ContentBrief
import com.example.data.SystemEvent
import com.example.ui.components.AetherGlassCard
import com.example.ui.components.GlassMetricCard
import com.example.ui.components.LiquidBackground
import com.example.ui.components.ObsidianBg
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: AetherViewModel) {
    val settings by viewModel.appSettings.collectAsState()
    val briefs by viewModel.contentBriefs.collectAsState()
    val events by viewModel.systemEvents.collectAsState()
    val snapshots by viewModel.engagementSnapshots.collectAsState()

    val pendingBriefs = briefs.filter { it.status != "published" }
    
    // Calculated stats
    val totalReach = snapshots.sumOf { it.reach }
    val totalLikes = snapshots.sumOf { it.likes }
    val totalComments = snapshots.sumOf { it.comments }

    var selectedBriefForDialog by remember { mutableStateOf<ContentBrief?>(null) }

    LiquidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            // Mini Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = settings?.projectName?.ifEmpty { "Aether Engine" } ?: "Aether Engine",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isConnected = !settings?.supabaseUrl.isNullOrEmpty()
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Emerald500 else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConnected) "Backplane active" else "Setup required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isSyncing by viewModel.isSyncing.collectAsState()
                    val isConnected = !settings?.supabaseUrl.isNullOrEmpty()
                    
                    if (isConnected) {
                        IconButton(
                            onClick = { viewModel.triggerSupabaseSync() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync with Supabase",
                                tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            if (settings?.supabaseUrl.isNullOrEmpty()) {
                AetherGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFF59E0B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Data Connection Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aether requires a valid Supabase connection to store and retrieve production data. Run the setup wizard to connect your backend.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.SETUP_WIZARD) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Run Setup Wizard")
                        }
                    }
                }
            } else {

            // Segmentation Control for Posting Mode
            Text(
                text = "POSTING CONTROL PIPELINE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("manual", "hybrid_auto").forEach { mode ->
                        val isSelected = settings?.postingMode == mode
                        val (label, bg, color) = when (mode) {
                            "manual" -> Triple("Manual", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
                            else -> Triple("Hybrid Auto", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) bg.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) bg else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.updateBrandSettings(
                                        projectName = settings?.projectName ?: "Aether Space",
                                        brandVoice = settings?.brandVoice ?: "Professional Innovator",
                                        audience = settings?.audience ?: "General",
                                        postingMode = mode,
                                        maxPostsPerDay = settings?.maxPostsPerDay ?: 2,
                                        dailySpendCap = settings?.dailySpendCap ?: 5.0
                                    )
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Quick Stats Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassMetricCard(
                    title = "Total Reach",
                    value = formatNumber(totalReach),
                    subValue = "+12%",
                    icon = { Icon(Icons.Default.TrendingUp, "Reach", tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.weight(1f)
                )
                GlassMetricCard(
                    title = "Likes & Reactions",
                    value = formatNumber(totalLikes),
                    subValue = "+8.4%",
                    icon = { Icon(Icons.Default.Favorite, "Engagement", tint = Color(0xFFEC4899)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Upcoming Deck Stream
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPCOMING DESIGN DECK (${pendingBriefs.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo(Screen.SCHEDULE) }) {
                    Text("View Schedule ↗", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (pendingBriefs.isEmpty()) {
                AetherGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Your content schedule is empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingBriefs) { brief ->
                        DashboardBriefCard(
                            brief = brief,
                            onActionClick = { selectedBriefForDialog = brief },
                            onApprove = { viewModel.approveBrief(brief.id) },
                            onPublish = { viewModel.publishBriefImmediate(brief.id) }
                        )
                    }
                }
            }

            // Monitor log
            Text(
                text = "CORE SYSTEM LOGS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                if (events.isEmpty()) {
                    Text(
                        text = "No system logs compiled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                } else {
                    events.take(5).forEach { event ->
                        SystemEventRow(event)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("View Workspace Settings Log", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            } // end of else block

        }

        // Action Dialog showing post details and edit options
        selectedBriefForDialog?.let { brief ->
            val isDark = isSystemInDarkTheme()
            AlertDialog(
                onDismissRequest = { selectedBriefForDialog = null },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                title = {
                    Text(
                        text = brief.topic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "CAPTION:",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = brief.caption,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "IMAGE PROMPT:",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = brief.imagePrompt,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        if (brief.imageUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = brief.imageUrl,
                                contentDescription = "Generated illustration",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Slate900 else Slate100),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.publishBriefImmediate(brief.id)
                                selectedBriefForDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Publish", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Now", fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardBriefCard(
    brief: ContentBrief,
    onActionClick: () -> Unit,
    onApprove: () -> Unit,
    onPublish: () -> Unit
) {
    val formatter = SimpleDateFormat("EEE hh:mm a", Locale.getDefault())
    val formattedTime = formatter.format(Date(brief.slotTime))
    val isDark = isSystemInDarkTheme()

    AetherGlassCard(
        modifier = Modifier
            .width(260.dp)
            .clickable { onActionClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Slate900 else Slate100)
        ) {
            if (brief.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = brief.imageUrl,
                    contentDescription = brief.topic,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Score tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Score: ${(brief.predictedScore * 100).toInt()}%",
                    color = Emerald500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            // Time tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.60f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = formattedTime,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = brief.topic,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = brief.caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (brief.status) {
                "approved" -> {
                    Button(
                        onClick = onPublish,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("PUBLISH NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("APPROVE SLOT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemEventRow(event: SystemEvent) {
    val color = when (event.severity) {
        "error" -> Color(0xFFEF4444)
        "warn" -> Color(0xFFF59E0B)
        else -> Emerald500
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )
            Text(
                text = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

fun formatNumber(num: Int): String {
    return if (num >= 1000) {
        "${"%.1f".format(num / 1000.0)}K"
    } else {
        num.toString()
    }
}
