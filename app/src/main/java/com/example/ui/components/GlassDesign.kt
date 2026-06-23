package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Glass design colors
val ObsidianBg = Color(0xFF070913)
val CoreElectricBlue = Color(0xFF3B82F6)
val GlowPremiumPink = Color(0xFFEC4899)
val GlassSurfaceColor = Color(0x12FFFFFF)
val GlassBorderColor = Color(0x28FFFFFF)
val GlassHighlightColor = Color(0x3FFFFFFF)

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .drawBehind {
                // Draw floating neon orbs representing the cybernetic "liquid" background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CoreElectricBlue.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(x = size.width * 0.15f, y = size.height * 0.25f),
                        radius = size.width * 0.65f,
                        tileMode = TileMode.Clamp
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowPremiumPink.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(x = size.width * 0.85f, y = size.height * 0.70f),
                        radius = size.width * 0.75f,
                        tileMode = TileMode.Clamp
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF10B981).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(x = size.width * 0.50f, y = size.height * 0.90f),
                        radius = size.width * 0.45f
                    )
                )
            }
    ) {
        content()
    }
}

@Composable
fun AetherGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassSurfaceColor)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassHighlightColor,
                        GlassBorderColor,
                        GlassHighlightColor.copy(alpha = 0.05f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 400f)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassMetricCard(
    title: String,
    value: String,
    subValue: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    AetherGlassCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1F3B82F6))
                    .border(1.dp, Color(0x3D3B82F6), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                if (subValue.isNotEmpty()) {
                    Text(
                        text = subValue,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}
