package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WaterBarChart
import com.example.ui.theme.AccentGold
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveGlow
import com.example.ui.theme.SleekSlateBg
import com.example.ui.theme.SleekSlateBorder
import com.example.ui.viewmodel.WaterViewModel
import com.example.data.model.WaterDrink
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val drinks by viewModel.allDrinks.collectAsState()
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsState()
    val useLiters by viewModel.useLiters.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()
    val totalLiters by viewModel.totalLitersConsumed.collectAsState()
    val totalCompletions by viewModel.totalGoalsCompleted.collectAsState()
    val weeklyData by viewModel.weeklyChartData.collectAsState()

    // 1. Calculate historical metrics
    val totalDaysWithData = remember(drinks) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        drinks.map { sdf.format(Date(it.timestamp)) }.distinct().size.coerceAtLeast(1)
    }
    
    val averageIntakeMl = remember(drinks, totalDaysWithData) {
        val totalMl = drinks.sumOf { it.amountMl }
        (totalMl.toDouble() / totalDaysWithData)
    }

    // 2. Generate monthly calendar grid
    val calendarDays = remember(drinks, dailyGoalMl) {
        generateCalendarDays(drinks, dailyGoalMl)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Hero Title
        item {
            Text(
                text = "Hydration Stats",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // 1. Grid of Core Metrics (Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Consumed",
                    value = String.format("%.1f L", totalLiters),
                    icon = Icons.Default.LocalDrink,
                    iconColor = WaterWaveDeep,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Current Streak",
                    value = "$currentStreak days",
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = AccentGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Daily Average",
                    value = if (useLiters) {
                        String.format("%.2f L", averageIntakeMl / 1000.0)
                    } else {
                        "${averageIntakeMl.toInt()} ml"
                    },
                    icon = Icons.Default.Analytics,
                    iconColor = WaterWaveGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Best Streak",
                    value = "$bestStreak days",
                    icon = Icons.Default.EmojiEvents,
                    iconColor = AccentGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Weekly Bar Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSlateBg
                ),
                border = BorderStroke(1.dp, SleekSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Hydration Cycle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Your water intake logs for the past 7 days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    WaterBarChart(
                        weeklyData = weeklyData,
                        dailyGoalMl = dailyGoalMl,
                        useLiters = useLiters,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Monthly Habit Calendar Grid
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
                        Column {
                            Text(
                                text = "Monthly Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Days completed: $totalCompletions this month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Monthly Habits",
                            tint = WaterWaveDeep
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendar Week Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid (7 columns chunks)
                    calendarDays.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { calDay ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (calDay.dayNumber > 0) {
                                        val isCompleted = calDay.isCompleted
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .then(
                                                    if (isCompleted) {
                                                        Modifier.background(
                                                            Brush.radialGradient(
                                                                colors = listOf(WaterWaveGlow.copy(alpha = 0.3f), WaterWaveDeep.copy(alpha = 0.1f))
                                                            )
                                                        )
                                                    } else if (calDay.isToday) {
                                                        Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isCompleted) {
                                                        WaterWaveGlow
                                                    } else if (calDay.isToday) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${calDay.dayNumber}",
                                                fontSize = 12.sp,
                                                fontWeight = if (calDay.isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCompleted) {
                                                    WaterWaveDeep
                                                } else if (calDay.isToday) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Backdate Sample Data Dialog option (for development test aesthetics)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WaterWaveDeep.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, WaterWaveDeep.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Demo Mode: Fast-Track History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WaterWaveDeep
                    )
                    Text(
                        text = "Want to see how your Streaks and Weekly/Monthly graphs look? Inject backdated logs for the past 10 days instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            // Inject logs backdated from day 1 to day 10
                            for (i in 1..10) {
                                // Add 2-3 liters backdated
                                viewModel.addDrinkBackdated(1200, i)
                                viewModel.addDrinkBackdated(1500, i)
                                if (i % 2 == 0) {
                                    // Make some days complete the full 4L goal
                                    viewModel.addDrinkBackdated(1300, i)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaterWaveDeep),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Populate 10 Days History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SleekSlateBg
        ),
        border = BorderStroke(1.dp, SleekSlateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

data class CalendarDay(
    val dayNumber: Int,
    val isCompleted: Boolean,
    val isToday: Boolean
)

private fun generateCalendarDays(
    drinks: List<WaterDrink>,
    goalMl: Double
): List<CalendarDay> {
    val list = mutableListOf<CalendarDay>()
    
    val calendar = Calendar.getInstance()
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
    val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Set to first of month to find start day offset
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    val emptySlotsOffset = firstDayOfWeek - 1 // Sunday start offset

    // 1. Add empty slot placeholders
    for (i in 0 until emptySlotsOffset) {
        list.add(CalendarDay(0, false, false))
    }

    // 2. Parse database entries to identify completions
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val groupedMap = mutableMapOf<String, Double>()
    drinks.forEach { drink ->
        val dateStr = sdf.format(Date(drink.timestamp))
        val currentAmount = groupedMap[dateStr] ?: 0.0
        groupedMap[dateStr] = currentAmount + drink.amountMl
    }

    val calHelper = Calendar.getInstance()
    
    // 3. Add actual days
    for (day in 1..totalDaysInMonth) {
        calHelper.set(Calendar.DAY_OF_MONTH, day)
        val dateStr = sdf.format(calHelper.time)
        val totalOnDay = groupedMap[dateStr] ?: 0.0
        val isCompleted = totalOnDay >= goalMl
        val isToday = day == todayDay && isSameMonthAndYear(calHelper, Calendar.getInstance())

        list.add(CalendarDay(day, isCompleted, isToday))
    }

    return list
}

private fun isSameMonthAndYear(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
           cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}
