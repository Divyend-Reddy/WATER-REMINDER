package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WaterWaveAnimation
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveGlow
import com.example.ui.theme.WaterWaveLight
import com.example.ui.theme.SleekCardBgStart
import com.example.ui.theme.SleekCardBgEnd
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekSlateBg
import com.example.ui.theme.SleekSlateBorder
import com.example.ui.theme.SleekMotivationalBg
import com.example.ui.theme.SleekMotivationalBorder
import com.example.ui.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val todayTotalMl by viewModel.todayTotalMl.collectAsState()
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsState()
    val remainingMl by viewModel.remainingMl.collectAsState()
    val useLiters by viewModel.useLiters.collectAsState()
    val isAhead by viewModel.isAheadOfSchedule.collectAsState()
    val checkpoints by viewModel.dailyCheckpoints.collectAsState()
    val todayDrinks by viewModel.todayDrinks.collectAsState()
    val completionPercentage by viewModel.completionPercentage.collectAsState()

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Fluid Bottle Hero Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .testTag("hero_bottle_card"),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(1.dp, SleekCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SleekCardBgStart, SleekCardBgEnd)
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fluid animated bottle on the left
                        WaterWaveAnimation(
                            fillPercentage = completionPercentage,
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Metrics on the right
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Today's Intake",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (useLiters) {
                                        String.format("%.2f", todayTotalMl / 1000.0)
                                    } else {
                                        "${todayTotalMl.toInt()}"
                                    },
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("current_intake_label")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (useLiters) "L" else "ml",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WaterWaveDeep,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Text(
                                text = if (useLiters) {
                                    String.format("Goal: %.1fL", dailyGoalMl / 1000.0)
                                } else {
                                    "Goal: ${dailyGoalMl.toInt()}ml"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Progress percentage block
                            Text(
                                text = String.format("%.0f%%", completionPercentage * 100f),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = WaterWaveGlow
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (remainingMl > 0) {
                                Text(
                                    text = if (useLiters) {
                                        String.format("%.2fL left", remainingMl / 1000.0)
                                    } else {
                                        "${remainingMl.toInt()}ml left"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            } else {
                                Text(
                                    text = "Daily Goal Met! 🎉",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentSuccess
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Schedule Tracking Card (Ahead or Behind)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(
                    1.dp,
                    SleekSlateBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAhead) AccentSuccess.copy(alpha = 0.15f) else AccentWarning.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isAhead) AccentSuccess.copy(alpha = 0.3f) else AccentWarning.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Schedule Status",
                            tint = if (isAhead) AccentSuccess else AccentWarning
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isAhead) "Ahead of Schedule" else "Behind Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAhead) AccentSuccess else AccentWarning
                        )
                        Text(
                            text = if (isAhead) {
                                "Great work! You are pacing well to hit your hydration goals today."
                            } else {
                                "Try drinking a glass of water now to catch up with your timeline!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 3. Quick Add Buttons Section
        item {
            Column {
                Text(
                    text = "Quick Hydrate",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val addOptions = listOf(
                        Pair(100, "+100ml"),
                        Pair(250, "+250ml"),
                        Pair(500, "+500ml"),
                        Pair(1000, "+1L")
                    )

                    addOptions.forEach { (amount, label) ->
                        val isProminent = amount == 500
                        Button(
                            onClick = { viewModel.addDrink(amount) },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("add_${amount}ml_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isProminent) WaterWaveDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (isProminent) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = CircleShape,
                            border = BorderStroke(
                                width = if (isProminent) 1.5.dp else 1.dp,
                                color = if (isProminent) WaterWaveGlow else SleekSlateBorder
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.WaterDrop,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isProminent) Color.White else WaterWaveLight
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Daily Schedule Progress
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
                    Text(
                        text = "Intraday Schedule Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    checkpoints.forEachIndexed { index, check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (check.isCompleted) {
                                    Icons.Outlined.CheckCircle
                                } else {
                                    Icons.Outlined.Circle
                                },
                                contentDescription = if (check.isCompleted) "Done" else "Pending",
                                tint = if (check.isCompleted) WaterWaveGlow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = check.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (check.isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = check.timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }

                            Text(
                                text = if (useLiters) {
                                    String.format("%.1fL", check.targetMl / 1000.0)
                                } else {
                                    "${check.targetMl.toInt()}ml"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (check.isCompleted) WaterWaveDeep else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        if (index < checkpoints.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                modifier = Modifier.padding(start = 36.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Timeline History Header & Action (Undo)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intake History Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (todayDrinks.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.undoLastDrink() },
                        modifier = Modifier.testTag("undo_button"),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Undo Last", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 6. Timeline List Items
        if (todayDrinks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SleekSlateBg
                    ),
                    border = BorderStroke(1.dp, SleekSlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalDrink,
                            contentDescription = "No drinks yet",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hydration logs yet today",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Use the buttons above to log your water consumption.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(todayDrinks, key = { it.id }) { drink ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SleekSlateBg
                    ),
                    border = BorderStroke(1.dp, SleekSlateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WaterWaveDeep.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalDrink,
                                contentDescription = null,
                                tint = WaterWaveDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Water Hydration",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = try {
                                    timeFormat.format(Date(drink.timestamp))
                                } catch (e: Exception) {
                                    ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        Text(
                            text = if (useLiters) {
                                String.format("%.2fL", drink.amountMl / 1000.0)
                            } else {
                                "+${drink.amountMl}ml"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WaterWaveDeep
                        )
                    }
                }
            }
        }
    }
}
