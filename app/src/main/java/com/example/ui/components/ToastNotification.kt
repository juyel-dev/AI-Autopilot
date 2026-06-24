package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ToastType {
    SUCCESS, ERROR, INFO, WARNING
}

data class ToastMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: ToastType = ToastType.INFO,
    val durationMs: Long = 4000L
)

@Composable
fun ToastNotification(
    toast: ToastMessage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, accentColor, bgGradient) = when (toast.type) {
        ToastType.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF10B981), // Emerald green
            listOf(Color(0x1F10B981), Color(0x0A10B981))
        )
        ToastType.ERROR -> Triple(
            Icons.Default.Error,
            Color(0xFFEF4444), // Crimson/Red
            listOf(Color(0x1FEF4444), Color(0x0AEF4444))
        )
        ToastType.WARNING -> Triple(
            Icons.Default.Warning,
            Color(0xFFF59E0B), // Amber yellow
            listOf(Color(0x1FF59E0B), Color(0x0AF59E0B))
        )
        ToastType.INFO -> Triple(
            Icons.Default.Info,
            Color(0xFF3B82F6), // Cyber Blue
            listOf(Color(0x1F3B82F6), Color(0x0A3B82F6))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("toast_notification_${toast.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = bgGradient
                    )
                )
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) // theme-adaptive solid background
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.5f),
                            accentColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = toast.type.name,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (toast.type) {
                        ToastType.SUCCESS -> "Success"
                        ToastType.ERROR -> "Error Encountered"
                        ToastType.WARNING -> "Warning"
                        ToastType.INFO -> "Notification"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = toast.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("toast_dismiss_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ToastOverlay(
    messages: List<ToastMessage>,
    onDismiss: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            messages.takeLast(3).forEach { toast ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it }
                    ) + fadeOut()
                ) {
                    ToastNotification(
                        toast = toast,
                        onDismiss = { onDismiss(toast.id) }
                    )
                }
            }
        }
    }
}
