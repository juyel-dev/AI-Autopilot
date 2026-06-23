package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CostEntry
import com.example.data.EngagementSnapshot
import com.example.ui.components.AetherGlassCard
import com.example.ui.components.LiquidBackground
import com.example.ui.viewmodel.AetherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: AetherViewModel) {
    val snapshots by viewModel.engagementSnapshots.collectAsState()
    val costEntries by viewModel.costEntries.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    val currentSpendCap = settings?.dailySpendCap ?: 5.0
    val costProgress = if (currentSpendCap > 0) (totalCost / currentSpendCap).toFloat().coerceIn(0f, 1f) else 0f

    LiquidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            Text(
                text = "Performance Maps",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Live analytics from synced Facebook clusters",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Segment 1: Reach & Likes Graph
            Text(
                text = "DAILY POST REACH TREND",
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
                if (snapshots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting first published post to compile charts...",
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    AnalyticsCustomChart(snapshots = snapshots)
                }
            }

            // Segment 2: Cost & Budgets
            Text(
                text = "AI COMPUTATIONAL EXPENSES",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Spend Progress
                AetherGlassCard(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(160.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                            CircularProgressIndicator(
                                progress = { costProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFEC4899),
                                trackColor = Color(0x12FFFFFF),
                                strokeWidth = 8.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$${"%.3f".format(totalCost)}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                               )
                                Text(
                                    text = "Spent",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Daily Limit: $${"%.1f".format(currentSpendCap)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Tokens & Queries
                AetherGlassCard(
                    modifier = Modifier
                        .weight(1.7f)
                        .height(160.dp)
                ) {
                    val totalTokens = costEntries.sumOf { it.inputTokens + it.outputTokens }
                    Column(
                        verticalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column {
                            Text(
                                text = "Luminous Model Calls",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${costEntries.size} requests",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Aggregated Token Usage",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = formatNumber(totalTokens),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                }
            }

            // Segment 3: Spend Ledger
            Text(
                text = "DETAILED EXPENDITURE LEDGER",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                if (costEntries.isEmpty()) {
                    Text(
                        text = "No recorded generative transactions yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    costEntries.take(10).forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = entry.provider,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss a", Locale.getDefault()).format(Date(entry.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${"%.5f".format(entry.estimatedCost)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEC4899)
                                )
                                Text(
                                    text = "In: ${entry.inputTokens} | Out: ${entry.outputTokens}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0x0AFFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AnalyticsCustomChart(snapshots: List<EngagementSnapshot>) {
    val maxReach = (snapshots.maxOfOrNull { it.reach } ?: 1000).toFloat()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val itemCount = snapshots.size
            val barSpacing = width / (itemCount + 1)
            val barWidth = barSpacing * 0.45f

            // Draw horizontal reference lines
            val lineCount = 3
            for (i in 0..lineCount) {
                val yVal = height * (i.toFloat() / lineCount)
                drawLine(
                    color = Color(0x0DFFFFFF),
                    start = Offset(0f, yVal),
                    end = Offset(width, yVal),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bar graphs
            snapshots.forEachIndexed { index, snapshot ->
                val xPos = barSpacing * (index + 1)
                val barHeightRatio = if (maxReach > 0) snapshot.reach / maxReach else 0.1f
                val drawnBarHeight = height * barHeightRatio * 0.85f // cushion space

                // Reach Bar (Deep slate glow glassmorphism)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.2f))
                    ),
                    topLeft = Offset(xPos - barWidth / 2, height - drawnBarHeight),
                    size = Size(barWidth, drawnBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Secondary Overlay Indicator (Likes Dot Glow)
                val dotYPos = height - drawnBarHeight - 10.dp.toPx()
                drawCircle(
                    color = Color(0xFFEC4899),
                    radius = 3.dp.toPx(),
                    center = Offset(xPos, dotYPos.coerceAtLeast(4.dp.toPx()))
                )
            }
        }

        // Horizontal tags
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            snapshots.forEach { snapshot ->
                Text(
                    text = snapshot.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.40f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}
