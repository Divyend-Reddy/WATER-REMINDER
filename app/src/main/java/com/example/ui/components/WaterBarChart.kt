package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.DarkSecondary
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveLight
import com.example.ui.viewmodel.WeeklyDataPoint

@Composable
fun WaterBarChart(
    weeklyData: List<WeeklyDataPoint>,
    dailyGoalMl: Double,
    useLiters: Boolean,
    modifier: Modifier = Modifier
) {
    val barAnimations = remember { weeklyData.map { Animatable(0f) } }

    // Trigger sequential entry animations for each bar
    LaunchedEffect(weeklyData) {
        barAnimations.forEachIndexed { index, animatable ->
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 500,
                    delayMillis = index * 40
                )
            )
        }
    }

    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val textStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Reserve bottom padding for text labels
                val chartHeight = canvasHeight - 30f
                val paddingX = 40f
                val availableWidth = canvasWidth - (paddingX * 2)

                val barCount = weeklyData.size
                val barSpacing = availableWidth / (barCount * 1.5f)
                val barWidth = (availableWidth - (barSpacing * (barCount - 1))) / barCount

                // Find max amount to scale the graph height safely
                val maxDataAmount = (weeklyData.maxOfOrNull { it.amountMl } ?: 0.0)
                val scaleMax = maxOf(maxDataAmount, dailyGoalMl * 1.2)

                // 1. Draw horizontal daily goal target line (dashed line)
                val goalY = chartHeight - ((dailyGoalMl / scaleMax) * chartHeight).toFloat()
                if (goalY in 0f..chartHeight) {
                    drawLine(
                        color = Color(0x664DAFFF),
                        start = Offset(paddingX, goalY),
                        end = Offset(canvasWidth - paddingX, goalY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                }

                // 2. Draw daily bars
                weeklyData.forEachIndexed { index, point ->
                    val progressRatio = (point.amountMl / scaleMax).toFloat()
                    val animatedRatio = progressRatio * (barAnimations.getOrNull(index)?.value ?: 1f)
                    
                    val barHeight = animatedRatio * chartHeight
                    val startX = paddingX + index * (barWidth + barSpacing)
                    val startY = chartHeight - barHeight

                    // Choose colors based on goal completion
                    val isGoalCompleted = point.amountMl >= dailyGoalMl
                    val barColors = if (isGoalCompleted) {
                        listOf(DarkSecondary, DarkPrimary)
                    } else {
                        listOf(WaterWaveLight, WaterWaveDeep)
                    }

                    // Background empty bar track to create surface depth
                    drawRoundRect(
                        color = Color(0x0A4FAFFF),
                        topLeft = Offset(startX, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Actual active intake filled bar
                    if (barHeight > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = barColors,
                                startY = startY,
                                endY = chartHeight
                            ),
                            topLeft = Offset(startX, startY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(12f, 12f)
                        )

                        // If daily goal reached, draw a small glowing bubble inside or on top
                        if (isGoalCompleted) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = 4f,
                                center = Offset(startX + (barWidth / 2), startY + 12f)
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Days row below the Canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Text(
                        text = point.dayName,
                        style = textStyle,
                        color = labelColor
                    )
                    Text(
                        text = if (useLiters) {
                            String.format("%.1fL", point.amountMl / 1000.0)
                        } else {
                            "${point.amountMl.toInt()}ml"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = labelColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
