package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.ui.theme.*

// Fallback glass design colors
val ObsidianBg = Color(0xFF0F172A)
val CoreElectricBlue = Color(0xFF3B82F6)
val GlowPremiumPink = Color(0xFFEC4899)

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Slate900 else Color.White
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                if (isDark) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Blue500.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(x = size.width * 0.15f, y = size.height * 0.25f),
                            radius = size.width * 0.65f,
                            tileMode = TileMode.Clamp
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Emerald500.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(x = size.width * 0.85f, y = size.height * 0.70f),
                            radius = size.width * 0.75f,
                            tileMode = TileMode.Clamp
                        )
                    )
                } else {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Blue500.copy(alpha = 0.07f), Color.Transparent),
                            center = Offset(x = size.width * 0.20f, y = size.height * 0.15f),
                            radius = size.width * 0.55f,
                            tileMode = TileMode.Clamp
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Emerald500.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(x = size.width * 0.80f, y = size.height * 0.80f),
                            radius = size.width * 0.60f,
                            tileMode = TileMode.Clamp
                        )
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun AetherGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Slate800 else Slate50
    val borderColor = if (isDark) Slate700 else Slate200

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(cardBg)
            .border(
                width = borderWidth,
                color = borderColor,
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
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Slate900
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Slate600

    AetherGlassCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x1F3B82F6) else Color(0xFFEFF6FF))
                    .border(1.dp, if (isDark) Color(0x3D3B82F6) else Color(0xFFDBEAFE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = subTextColor
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
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
