package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LiquidBackground
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AetherViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent, // Let the custom liquid background bleed through
                    bottomBar = {
                        if (currentScreen != Screen.SETUP_WIZARD) {
                            FloatingGlassBottomBar(
                                currentScreen = currentScreen,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (currentScreen != Screen.SETUP_WIZARD) 80.dp else 0.dp)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) togetherWith 
                                fadeOut(animationSpec = androidx.compose.animation.core.tween(220))
                            },
                            label = "MainScreenTransition"
                        ) { target ->
                            when (target) {
                                Screen.SETUP_WIZARD -> SetupWizardScreen(viewModel = viewModel)
                                Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                                Screen.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                                Screen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingGlassBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Safely offsets our custom capsule below system gesture overlays!
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Frosted capsule wrapper representing high end glassmorphism
        Row(
            modifier = Modifier
                .width(340.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x1F111827))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x52FFFFFF), Color(0x14FFFFFF))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple(Screen.DASHBOARD, Icons.Default.Dashboard, "Home"),
                Triple(Screen.SCHEDULE, Icons.Default.DateRange, "Schedule"),
                Triple(Screen.ANALYTICS, Icons.Default.BarChart, "Analytics"),
                Triple(Screen.SETTINGS, Icons.Default.Settings, "Secrets")
            ).forEach { (screen, icon, label) ->
                val isSelected = currentScreen == screen
                val selectionColor = when (screen) {
                    Screen.DASHBOARD -> Color(0xFF3B82F6)   // Neon Blue
                    Screen.SCHEDULE -> Color(0xFF10B981)    // Emerald Green
                    Screen.ANALYTICS -> Color(0xFFEC4899)   // Hot Pink
                    else -> Color(0xFF8B5CF6)               // Cyber Purple
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onNavigate(screen) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) selectionColor else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(if (isSelected) 26.dp else 22.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(selectionColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
