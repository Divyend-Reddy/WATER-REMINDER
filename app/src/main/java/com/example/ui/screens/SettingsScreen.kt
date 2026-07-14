package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveGlow
import com.example.ui.theme.SleekSlateBg
import com.example.ui.theme.SleekSlateBorder
import com.example.ui.viewmodel.WaterViewModel

@Composable
fun SettingsScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drinks by viewModel.allDrinks.collectAsState()
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsState()
    val useLiters by viewModel.useLiters.collectAsState()
    val darkModeSetting by viewModel.darkModeSetting.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // 1. Daily Hydration Goal Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_goal_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(1.dp, SleekSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Goal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure your target hydration intake",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = WaterWaveDeep
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slider representation
                    Text(
                        text = if (useLiters) {
                            String.format("%.1f L", dailyGoalMl / 1000.0)
                        } else {
                            "${dailyGoalMl.toInt()} ml"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WaterWaveDeep
                    )

                    Slider(
                        value = dailyGoalMl.toFloat(),
                        onValueChange = { viewModel.setDailyGoal(it.toDouble()) },
                        valueRange = 1000f..8000f,
                        steps = 13, // increments of 500ml
                        colors = SliderDefaults.colors(
                            thumbColor = WaterWaveDeep,
                            activeTrackColor = WaterWaveGlow,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("goal_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Goal preset selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2000.0, 3000.0, 4000.0, 5000.0).forEach { amount ->
                            val label = if (useLiters) "${(amount / 1000.0).toInt()}L" else "${amount.toInt()}ml"
                            val isSelected = dailyGoalMl == amount
                            OutlinedButton(
                                onClick = { viewModel.setDailyGoal(amount) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) WaterWaveDeep else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) WaterWaveDeep.copy(alpha = 0.08f) else Color.Transparent,
                                    contentColor = if (isSelected) WaterWaveDeep else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Local Notifications Configurations
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(1.dp, SleekSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intelligent Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Get motivational prompts during the day to stay on track. Silenced automatically once goal is hit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                viewModel.triggerTestNotification()
                                Toast.makeText(context, "Testing reminder notification! Check status bar.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("test_notification_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WaterWaveDeep.copy(alpha = 0.1f),
                                contentColor = WaterWaveDeep
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Local Notification Signal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Theme & Unit Preference Swappings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(1.dp, SleekSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Units Toggling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Volume Units",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Show intake as Liters (L) or Milliliters (ml)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // ml / L Toggle Row Button
                        Row(
                            modifier = Modifier.width(100.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setUseLiters(false) },
                                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!useLiters) WaterWaveDeep.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (!useLiters) WaterWaveDeep else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, if (!useLiters) WaterWaveDeep else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                            ) {
                                Text("ml", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.setUseLiters(true) },
                                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (useLiters) WaterWaveDeep.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (useLiters) WaterWaveDeep else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, if (useLiters) WaterWaveDeep else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                            ) {
                                Text("L", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Enforced Theme Toggles (Light vs Dark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Interface Theme",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose Light, Dark, or follow system default",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // 3-way toggle button
                        Row(modifier = Modifier.height(36.dp)) {
                            listOf(
                                Triple(0, "Auto", Icons.Default.BrightnessAuto),
                                Triple(1, "Light", Icons.Default.LightMode),
                                Triple(2, "Dark", Icons.Default.DarkMode)
                            ).forEach { (mode, label, icon) ->
                                val isSelected = darkModeSetting == mode
                                OutlinedButton(
                                    onClick = { viewModel.setDarkMode(mode) },
                                    shape = when (mode) {
                                        0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                        2 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                        else -> RoundedCornerShape(0.dp)
                                    },
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) WaterWaveDeep.copy(alpha = 0.1f) else Color.Transparent,
                                        contentColor = if (isSelected) WaterWaveDeep else MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) WaterWaveDeep else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                                ) {
                                    Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. History Export & Factory Reset Data
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(1.dp, SleekSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Export History CSV
                    Button(
                        onClick = {
                            val csvData = viewModel.getExportHistoryString(drinks)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("WaterFlow History CSV", csvData)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "History CSV successfully copied to clipboard!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_csv_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export History (Copy CSV)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Dangerous Factory Reset Button
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("clear_history_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Data & Logs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Factory Reset Confirmation Alert Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Purge Hydration History?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "This action is completely permanent and offline. You will lose your daily logs, custom hydration target settings, unlocked achievements badges, and streaks. Are you absolutely sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        Toast.makeText(context, "Database cleared successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
