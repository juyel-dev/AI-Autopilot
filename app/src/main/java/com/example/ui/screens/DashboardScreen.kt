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
                        text = settings?.projectName ?: "Aether Engine",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Backplane active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
                
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x1BFFFFFF), CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            // Segmentation Control for Posting Mode
            Text(
                text = "POSTING CONTROL PIPELINE",
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("manual", "hybrid", "full_auto").forEach { mode ->
                        val isSelected = settings?.postingMode == mode
                        val (label, bg, color) = when (mode) {
                            "manual" -> Triple("Manual", Color(0xFFF59E0B), Color.Black)
                            "hybrid" -> Triple("Hybrid AI", Color(0xFF3B82F6), Color.White)
                            else -> Triple("Full Auto", Color(0xFFEC4899), Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) bg.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) bg else Color(0x1AFFFFFF),
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
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
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
                    icon = { Icon(Icons.Default.TrendingUp, "Reach", tint = Color(0xFF3B82F6)) },
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
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo(Screen.SCHEDULE) }) {
                    Text("View Schedule ↗")
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
                        color = Color.White.copy(alpha = 0.5f),
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
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                if (events.isEmpty()) {
                    Text(
                        text = "No system logs compiled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                } else {
                    events.take(5).forEach { event ->
                        SystemEventRow(event)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0x0DFFFFFF)
                        )
                    }
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("View Workspace Settings Log")
                    }
                }
            }
        }

        // Action Dialog showing post details and edit options
        selectedBriefForDialog?.let { brief ->
            AlertDialog(
                onDismissRequest = { selectedBriefForDialog = null },
                containerColor = ObsidianBg,
                modifier = Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp)),
                title = {
                    Text(
                        text = brief.topic,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "CAPTION:",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = brief.caption,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "IMAGE PROMPT:",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = brief.imagePrompt,
                            color = Color.White.copy(alpha = 0.8f),
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
                                    .background(Color(0xFF0F172A)),
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Publish", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Now", fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.regenerateBrief(brief.id)
                                selectedBriefForDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Regen", fontSize = 12.sp)
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
                .background(Color(0xFF0F172A))
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
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            }

            // Score tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Score: ${(brief.predictedScore * 100).toInt()}%",
                    color = Color(0xFF34D399),
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
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = brief.caption,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("PUBLISH NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
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
        else -> Color(0xFF34D399)
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
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
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
