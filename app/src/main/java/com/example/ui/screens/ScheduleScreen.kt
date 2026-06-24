package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ContentBrief
import com.example.ui.components.AetherGlassCard
import com.example.ui.components.LiquidBackground
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: AetherViewModel) {
    val briefs by viewModel.contentBriefs.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var briefToEdit by remember { mutableStateOf<ContentBrief?>(null) }
    
    // Dialog inputs
    var newTopic by remember { mutableStateOf("") }
    var newCaption by remember { mutableStateOf("") }
    var newHashtags by remember { mutableStateOf("") }

    val settings by viewModel.appSettings.collectAsState()

    LiquidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Creative Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Rolling 7-Day Planning Deck",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                
                if (!settings?.supabaseUrl.isNullOrEmpty()) {
                    Button(
                        onClick = {
                            newTopic = ""
                            newCaption = ""
                            newHashtags = ""
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Brief")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Slot", fontSize = 12.sp)
                    }
                }
            }

            if (settings?.supabaseUrl.isNullOrEmpty()) {
                AetherGlassCard(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFF59E0B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Supabase Connection Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please run the setup wizard and configure your Supabase URL to activate the scheduling pipeline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (briefs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AetherGlassCard(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "No planned schedule detected. Tap the button above to insert a new calendar brief, or run Setup again to seed automated templates.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Cushion
                ) {
                    items(briefs) { brief ->
                        ScheduleBriefRow(
                            brief = brief,
                            onEdit = { briefToEdit = brief },
                            onApprove = { viewModel.approveBrief(brief.id) },
                            onDelete = { viewModel.deleteBrief(brief.id) },
                            onPublish = { viewModel.publishBriefImmediate(brief.id) }
                        )
                    }
                }
            }
        }

        // Add custom slot dialog
        if (showAddDialog) {
            var newImageUrl by remember { mutableStateOf("") }
            var newSlotTime by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) } // Next day
            val context = androidx.compose.ui.platform.LocalContext.current
            val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    newImageUrl = uri.toString()
                }
            }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF0F1223),
                modifier = Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp)),
                title = { Text("Design Custom Posting Slot", color = Color.White) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        WizardTextField(
                            value = newTopic,
                            onValueChange = { newTopic = it },
                            label = "Topic Headline",
                            placeholder = "e.g. Design Systems of 2026"
                        )
                        WizardTextField(
                            value = newCaption,
                            onValueChange = { newCaption = it },
                            label = "Caption Body",
                            placeholder = "Describe the vision..."
                        )
                        WizardTextField(
                            value = newHashtags,
                            onValueChange = { newHashtags = it },
                            label = "Hashtags List (comma separated)",
                            placeholder = "tech,design,aesthetics"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scheduled Date & Time", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = newSlotTime }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val newCal = java.util.Calendar.getInstance().apply {
                                                    set(year, month, dayOfMonth, hourOfDay, minute)
                                                }
                                                newSlotTime = newCal.timeInMillis
                                            },
                                            calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                            calendar.get(java.util.Calendar.MINUTE),
                                            false
                                        ).show()
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(SimpleDateFormat("EEE hh:mm a (MMM dd, yyyy)", Locale.getDefault()).format(Date(newSlotTime)))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Media", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { 
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (newImageUrl.isNotEmpty()) "Image Selected" else "Select Image from Gallery")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTopic.isNotEmpty()) {
                                viewModel.insertNewManualBrief(newTopic, newCaption, newHashtags, newImageUrl, newSlotTime)
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Create Brief")
                    }
                }
            )
        }

        // Edit brief dialog
        briefToEdit?.let { brief ->
            var editTopic by remember { mutableStateOf(brief.topic) }
            var editCaption by remember { mutableStateOf(brief.caption) }
            var editHashtags by remember { mutableStateOf(brief.hashtags) }
            var editImageUrl by remember { mutableStateOf(brief.imageUrl) }
            var editSlotTime by remember { mutableStateOf(brief.slotTime) }
            val context = androidx.compose.ui.platform.LocalContext.current
            
            val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    editImageUrl = uri.toString()
                }
            }

            AlertDialog(
                onDismissRequest = { briefToEdit = null },
                containerColor = Color(0xFF05070F),
                modifier = Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp)),
                title = { Text("Update Brief Manual Parameters", color = Color.White) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        WizardTextField(
                            value = editTopic,
                            onValueChange = { editTopic = it },
                            label = "Topic Headline",
                            placeholder = "Topic Headline"
                        )
                        WizardTextField(
                            value = editCaption,
                            onValueChange = { editCaption = it },
                            label = "Caption Body",
                            placeholder = "Caption"
                        )
                        WizardTextField(
                            value = editHashtags,
                            onValueChange = { editHashtags = it },
                            label = "Hashtags (comma separated)",
                            placeholder = "hashtags"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scheduled Date & Time", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = editSlotTime }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val newCal = java.util.Calendar.getInstance().apply {
                                                    set(year, month, dayOfMonth, hourOfDay, minute)
                                                }
                                                editSlotTime = newCal.timeInMillis
                                            },
                                            calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                            calendar.get(java.util.Calendar.MINUTE),
                                            false
                                        ).show()
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(SimpleDateFormat("EEE hh:mm a (MMM dd, yyyy)", Locale.getDefault()).format(Date(editSlotTime)))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Media", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { 
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (editImageUrl.isNotEmpty() && editImageUrl != brief.imageUrl) "New Image Selected" else "Change Image")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateBriefManually(
                                id = brief.id,
                                topic = editTopic,
                                caption = editCaption,
                                hashtags = editHashtags,
                                imageUrl = editImageUrl,
                                slotTime = editSlotTime
                            )
                            briefToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Save Update")
                    }
                }
            )
        }
    }
}

@Composable
fun ScheduleBriefRow(
    brief: ContentBrief,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit
) {
    val formatter = SimpleDateFormat("EEE hh:mm a (MMM dd)", Locale.getDefault())
    val formattedTime = formatter.format(Date(brief.slotTime))
    val isDark = isSystemInDarkTheme()

    val statusColor = when (brief.status) {
        "published" -> Emerald500
        "approved" -> MaterialTheme.colorScheme.primary
        "skipped" -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        else -> Color(0xFFF59E0B)
    }

    AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            
            // Status Pills
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = brief.status.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Left: Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Slate900 else Slate100)
            ) {
                if (brief.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = brief.imageUrl,
                        contentDescription = "Brief image thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), strokeWidth = 2.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Text details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = brief.topic,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = brief.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                if (brief.hashtags.isNotEmpty()) {
                    Text(
                        text = brief.hashtags.split(",").joinToString(" ") { "#$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (brief.status != "published") {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit Block", 
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), 
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                when (brief.status) {
                    "approved" -> {
                        Button(
                            onClick = onPublish,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Publish Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "skipped" -> {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Re-Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0x33EF4444)),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Delete", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Published to Facebook",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald500,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
