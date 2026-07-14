package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.os.VibrationEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.WaterApplication
import com.example.data.model.AppSetting
import com.example.data.model.WaterDrink
import com.example.data.repository.WaterRepository
import com.example.service.LocalNotificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: Float,
    val targetDescription: String,
    val iconName: String
)

data class WeeklyDataPoint(
    val dayName: String,
    val amountMl: Double,
    val dateLabel: String
)

data class DailyGoalProgress(
    val title: String,
    val targetMl: Double,
    val isCompleted: Boolean,
    val timeLabel: String
)

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository = (application as WaterApplication).repository
    private val context: Context = application.applicationContext

    // All drinks from the database
    val allDrinks: StateFlow<List<WaterDrink>> = repository.allDrinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Raw settings list
    val allSettings: StateFlow<List<AppSetting>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Parsed individual settings
    val dailyGoalMl: StateFlow<Double> = allSettings.map { settings ->
        settings.firstOrNull { it.key == "daily_goal" }?.value?.toDoubleOrNull() ?: 4000.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4000.0)

    val useLiters: StateFlow<Boolean> = allSettings.map { settings ->
        settings.firstOrNull { it.key == "use_liters" }?.value?.toBoolean() ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkModeSetting: StateFlow<Int> = allSettings.map { settings ->
        settings.firstOrNull { it.key == "dark_mode" }?.value?.toIntOrNull() ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificationsEnabled: StateFlow<Boolean> = allSettings.map { settings ->
        settings.firstOrNull { it.key == "notifications_enabled" }?.value?.toBoolean() ?: true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Today's drinks
    val todayDrinks: StateFlow<List<WaterDrink>> = allDrinks.map { drinks ->
        val todayStart = getStartOfDayTimestamp()
        val todayEnd = getEndOfDayTimestamp()
        drinks.filter { it.timestamp in todayStart..todayEnd }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's total consumption in ml
    val todayTotalMl: StateFlow<Double> = todayDrinks.map { drinks ->
        drinks.sumOf { it.amountMl }.toDouble()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Current hydration completion percentage
    val completionPercentage: StateFlow<Float> = combine(todayTotalMl, dailyGoalMl) { today, goal ->
        if (goal <= 0) 1f else (today / goal).toFloat().coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Remaining water in ml
    val remainingMl: StateFlow<Double> = combine(todayTotalMl, dailyGoalMl) { today, goal ->
        (goal - today).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4000.0)

    // Streak calculations
    val streaks: StateFlow<Pair<Int, Int>> = combine(allDrinks, dailyGoalMl) { drinks, goal ->
        calculateStreaks(drinks, goal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    val currentStreak: StateFlow<Int> = streaks.map { it.first }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestStreak: StateFlow<Int> = streaks.map { it.second }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Total liters consumed historically
    val totalLitersConsumed: StateFlow<Double> = allDrinks.map { drinks ->
        drinks.sumOf { it.amountMl }.toDouble() / 1000.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Goal Completion Count historically
    val totalGoalsCompleted: StateFlow<Int> = combine(allDrinks, dailyGoalMl) { drinks, goal ->
        val grouped = groupDrinksByDay(drinks)
        grouped.values.count { it >= goal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Schedule status: Ahead vs Behind schedule
    // Continuous hour-based target (7 AM to 11 PM)
    val isAheadOfSchedule: StateFlow<Boolean> = combine(todayTotalMl, dailyGoalMl) { today, goal ->
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val timeInHours = hour + (minutes / 60.0)

        val targetPercent = when {
            timeInHours < 7.0 -> 0.0
            timeInHours >= 23.0 -> 1.0
            else -> (timeInHours - 7.0) / 16.0 // Linear target from 7 AM to 11 PM
        }
        val targetMl = goal * targetPercent
        today >= targetMl
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Daily Goals Checkpoints:
    // Morning goal: 25% of goal before 12 PM
    // Afternoon goal: 50% of goal before 4 PM
    // Evening goal: 75% of goal before 8 PM
    // Final goal: 100% of goal before 11 PM
    val dailyCheckpoints: StateFlow<List<DailyGoalProgress>> = combine(todayTotalMl, dailyGoalMl) { today, goal ->
        listOf(
            DailyGoalProgress("Morning Goal", goal * 0.25, today >= goal * 0.25, "before 12 PM"),
            DailyGoalProgress("Afternoon Goal", goal * 0.50, today >= goal * 0.50, "before 4 PM"),
            DailyGoalProgress("Evening Goal", goal * 0.75, today >= goal * 0.75, "before 8 PM"),
            DailyGoalProgress("Final Goal", goal * 1.0, today >= goal, "before 11 PM")
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly Chart Data (past 7 days including today)
    val weeklyChartData: StateFlow<List<WeeklyDataPoint>> = combine(allDrinks, dailyGoalMl) { drinks, goal ->
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val nameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        
        val list = mutableListOf<WeeklyDataPoint>()
        val grouped = groupDrinksByDay(drinks)

        for (i in 6 downTo 0) {
            val calcCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = format.format(calcCalendar.time)
            val dayName = nameFormat.format(calcCalendar.time)
            val dateLabel = labelFormat.format(calcCalendar.time)
            val amount = grouped[dateStr] ?: 0.0
            list.add(WeeklyDataPoint(dayName, amount, dateLabel))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Achievements calculation
    val achievements: StateFlow<List<Achievement>> = combine(
        totalLitersConsumed,
        bestStreak,
        totalGoalsCompleted,
        todayTotalMl
    ) { totalLiters, bestStr, totalGoals, todayMl ->
        listOf(
            Achievement(
                id = "first_liter",
                name = "First Liter",
                description = "Kickstart your hydration habit.",
                isUnlocked = totalLiters >= 1.0,
                progress = (totalLiters / 1.0).toFloat().coerceIn(0f, 1f),
                targetDescription = "1 Liter Consumed",
                iconName = "water_drop"
            ),
            Achievement(
                id = "7_day_streak",
                name = "7-Day Streak",
                description = "Drink daily for a full week.",
                isUnlocked = bestStr >= 7,
                progress = (bestStr.toFloat() / 7f).coerceIn(0f, 1f),
                targetDescription = "7 Days Active",
                iconName = "calendar_today"
            ),
            Achievement(
                id = "30_day_streak",
                name = "30-Day Streak",
                description = "Build a solid life-long hydration habit.",
                isUnlocked = bestStr >= 30,
                progress = (bestStr.toFloat() / 30f).coerceIn(0f, 1f),
                targetDescription = "30 Days Active",
                iconName = "star"
            ),
            Achievement(
                id = "100_liters",
                name = "100 Liters",
                description = "Consume a massive barrel of water.",
                isUnlocked = totalLiters >= 100.0,
                progress = (totalLiters / 100.0).toFloat().coerceIn(0f, 1f),
                targetDescription = "100 Liters Consumed",
                iconName = "local_drink"
            ),
            Achievement(
                id = "hydration_master",
                name = "Hydration Master",
                description = "Complete your daily goal 10 times.",
                isUnlocked = totalGoals >= 10,
                progress = (totalGoals.toFloat() / 10f).coerceIn(0f, 1f),
                targetDescription = "10 Goal Completions",
                iconName = "workspace_premium"
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add a drink
    fun addDrink(amountMl: Int) {
        viewModelScope.launch {
            repository.addDrink(amountMl)
            triggerHapticFeedback()
            
            // Check if goal was reached right now, or schedule smart notifications
            if (notificationsEnabled.value) {
                val currentToday = todayTotalMl.value + amountMl
                val currentGoal = dailyGoalMl.value
                if (currentToday >= currentGoal) {
                    // Stop notifications/reminders or trigger completion celebratory notification!
                    LocalNotificationService.showReminderNotification(context, 0.0)
                }
            }
        }
    }

    // Support backdating for testing stats
    fun addDrinkBackdated(amountMl: Int, daysAgo: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            repository.addDrink(amountMl, calendar.timeInMillis)
        }
    }

    // Undo last drink
    fun undoLastDrink() {
        viewModelScope.launch {
            repository.undoLastDrink()
            triggerHapticFeedback()
        }
    }

    // Clear all history data
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            triggerHapticFeedback()
        }
    }

    // Save individual settings
    fun setDailyGoal(goalMl: Double) {
        viewModelScope.launch {
            repository.saveSetting("daily_goal", goalMl.toString())
        }
    }

    fun setUseLiters(useLiters: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("use_liters", useLiters.toString())
        }
    }

    fun setDarkMode(mode: Int) {
        viewModelScope.launch {
            repository.saveSetting("dark_mode", mode.toString())
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("notifications_enabled", enabled.toString())
        }
    }

    fun triggerTestNotification() {
        viewModelScope.launch {
            val rem = remainingMl.value
            LocalNotificationService.showReminderNotification(context, rem)
        }
    }

    // Export history helper (returns formatted CSV string)
    fun getExportHistoryString(drinks: List<WaterDrink>): String {
        val builder = StringBuilder()
        builder.append("ID,Amount (ml),Timestamp,Readable Time\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        drinks.forEach {
            val timeStr = sdf.format(Date(it.timestamp))
            builder.append("${it.id},${it.amountMl},${it.timestamp},\"$timeStr\"\n")
        }
        return builder.toString()
    }

    // --- Private Helper Methods ---

    private fun getStartOfDayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun groupDrinksByDay(drinks: List<WaterDrink>): Map<String, Double> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val groupedMap = mutableMapOf<String, Double>()
        drinks.forEach { drink ->
            val dayStr = format.format(Date(drink.timestamp))
            val currentAmount = groupedMap[dayStr] ?: 0.0
            groupedMap[dayStr] = currentAmount + drink.amountMl
        }
        return groupedMap
    }

    private fun calculateStreaks(drinks: List<WaterDrink>, goal: Double): Pair<Int, Int> {
        val grouped = groupDrinksByDay(drinks)
        if (grouped.isEmpty()) return Pair(0, 0)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayCalendar = Calendar.getInstance()
        val todayStr = format.format(todayCalendar.time)

        // Find best streak historically
        val sortedDates = grouped.keys.sorted().reversed()
        var currentStreakCount = 0
        var bestStreakCount = 0
        var tempStreak = 0

        // Calculate historic best streak
        val sortedDatesAsc = grouped.keys.sorted()
        var lastCalendar: Calendar? = null

        sortedDatesAsc.forEach { dateStr ->
            val date = format.parse(dateStr) ?: return@forEach
            val dateCalendar = Calendar.getInstance().apply { time = date }
            val amount = grouped[dateStr] ?: 0.0

            if (amount >= goal) {
                if (lastCalendar == null) {
                    tempStreak = 1
                } else {
                    val diff = getDaysBetween(lastCalendar!!, dateCalendar)
                    if (diff == 1) {
                        tempStreak++
                    } else if (diff > 1) {
                        if (tempStreak > bestStreakCount) bestStreakCount = tempStreak
                        tempStreak = 1
                    }
                }
                lastCalendar = dateCalendar
            } else {
                if (tempStreak > bestStreakCount) bestStreakCount = tempStreak
                tempStreak = 0
                lastCalendar = null
            }
        }
        if (tempStreak > bestStreakCount) bestStreakCount = tempStreak

        // Calculate current streak
        var checkCalendar = Calendar.getInstance()
        var checkDateStr = format.format(checkCalendar.time)
        val todayAmount = grouped[checkDateStr] ?: 0.0

        if (todayAmount >= goal) {
            currentStreakCount = 1
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            checkDateStr = format.format(checkCalendar.time)
            while ((grouped[checkDateStr] ?: 0.0) >= goal) {
                currentStreakCount++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
                checkDateStr = format.format(checkCalendar.time)
            }
        } else {
            // If today hasn't met the goal yet, check if yesterday did
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            checkDateStr = format.format(checkCalendar.time)
            if ((grouped[checkDateStr] ?: 0.0) >= goal) {
                currentStreakCount = 1
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
                checkDateStr = format.format(checkCalendar.time)
                while ((grouped[checkDateStr] ?: 0.0) >= goal) {
                    currentStreakCount++
                    checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    checkDateStr = format.format(checkCalendar.time)
                }
            } else {
                currentStreakCount = 0
            }
        }

        val finalBest = maxOf(bestStreakCount, currentStreakCount)
        return Pair(currentStreakCount, finalBest)
    }

    private fun getDaysBetween(cal1: Calendar, cal2: Calendar): Int {
        val c1 = Calendar.getInstance().apply {
            timeInMillis = cal1.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val c2 = Calendar.getInstance().apply {
            timeInMillis = cal2.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val msDiff = c2.timeInMillis - c1.timeInMillis
        return (msDiff / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(40)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
