package com.example.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.WaterApplication
import kotlin.random.Random

object LocalNotificationService {

    private val MOTIVATIONAL_MESSAGES = listOf(
        "💧 Time to hydrate and refresh your mind!",
        "💪 Creatine works best when you're hydrated.",
        "✨ Keep moving! Your future self will thank you.",
        "🙌 Your future self will thank you for drinking water.",
        "🌊 Stay fluid, stay focused, stay healthy!",
        "⚡ Energy levels dropping? Try drinking a glass of water!",
        "🧠 Dehydration shrinks your brain. Keep it sharp!",
        "💧 Sip, sip, hurray! Let's take a quick water break."
    )

    fun getRandomMotivationalMessage(remainingMl: Double): String {
        if (remainingMl <= 0) {
            return "🎉 Daily hydration goal achieved! Keep up the amazing work!"
        }
        val liters = String.format("%.1f", remainingMl / 1000.0)
        val list = MOTIVATIONAL_MESSAGES + listOf(
            "💧 Only ${liters}L left to complete your goal today!",
            "⚡ You've got this! Just ${liters}L left to reach your daily goal."
        )
        return list[Random.nextInt(list.size)]
    }

    fun showReminderNotification(context: Context, remainingMl: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val message = getRandomMotivationalMessage(remainingMl)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, WaterApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("WaterFlow")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(1001, notification)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
