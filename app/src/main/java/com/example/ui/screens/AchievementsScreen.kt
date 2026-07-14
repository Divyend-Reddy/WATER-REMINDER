package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveGlow
import com.example.ui.theme.SleekSlateBg
import com.example.ui.theme.SleekSlateBorder
import com.example.ui.viewmodel.Achievement
import com.example.ui.viewmodel.WaterViewModel

@Composable
fun AchievementsScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val achievements by viewModel.achievements.collectAsState()

    val unlockedCount = remember(achievements) {
        achievements.count { it.isUnlocked }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Achievements Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achievements_summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(1.dp, SleekSlateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = "Milestones Unlocked",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaterWaveDeep
                        )
                        Text(
                            text = "You've unlocked $unlockedCount out of ${achievements.size} goals. Complete daily goals to collect them all!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Trophy / percentage circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(AccentGold.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                            .border(2.dp, AccentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "$unlockedCount/${achievements.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Badges List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // List of Achievements
        items(achievements, key = { it.id }) { ach ->
            val animatedProgress by animateFloatAsState(
                targetValue = ach.progress,
                animationSpec = tween(600),
                label = "Progress"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achievement_card_${ach.id}"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (ach.isUnlocked) AccentGold.copy(alpha = 0.45f) else SleekSlateBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge Icon Circle (Glow gold if unlocked, dull gray if locked)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (ach.isUnlocked) {
                                    AccentGold.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (ach.isUnlocked) AccentGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = mapIconNameToVector(ach.iconName),
                            contentDescription = ach.name,
                            tint = if (ach.isUnlocked) AccentGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ach.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )

                            if (ach.isUnlocked) {
                                Badge(
                                    containerColor = AccentGold.copy(alpha = 0.15f),
                                    contentColor = AccentGold,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = "UNLOCKED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        // Progress indicator bar for achievement
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (ach.isUnlocked) AccentGold else WaterWaveDeep,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )

                            Text(
                                text = if (ach.isUnlocked) {
                                    "100%"
                                } else {
                                    ach.targetDescription
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) AccentGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun mapIconNameToVector(name: String): ImageVector {
    return when (name) {
        "water_drop" -> Icons.Default.WaterDrop
        "calendar_today" -> Icons.Default.CalendarToday
        "star" -> Icons.Default.Star
        "local_drink" -> Icons.Default.LocalDrink
        "workspace_premium" -> Icons.Default.WorkspacePremium
        else -> Icons.Default.EmojiEvents
    }
}
