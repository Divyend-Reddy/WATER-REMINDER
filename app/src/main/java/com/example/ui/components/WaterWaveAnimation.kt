package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveLight
import com.example.ui.theme.WaterWaveGlow
import kotlin.math.sin

@Composable
fun WaterWaveAnimation(
    fillPercentage: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    // Animate the fill percentage smoothly when it changes
    val animatedFill = animateFloatAsState(
        targetValue = fillPercentage.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WaterLevel"
    )

    // Infinite wave phase animations for continuous fluid simulation
    val infiniteTransition = rememberInfiniteTransition(label = "Waves")
    
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave1"
    )

    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave2"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Create a beautiful bottle/flask silhouette path
            val bottlePath = Path().apply {
                val neckWidth = width * 0.45f
                val neckHeight = height * 0.15f
                val bodyRadius = width * 0.35f
                val cornerRadius = width * 0.15f

                // Top lip of the flask/bottle
                moveTo((width - neckWidth) / 2, neckHeight * 0.5f)
                lineTo((width + neckWidth) / 2, neckHeight * 0.5f)
                
                // Neck down
                lineTo((width + neckWidth) / 2, neckHeight)
                
                // Shoulder sweep out
                cubicTo(
                    (width + neckWidth) / 2, neckHeight + (height * 0.08f),
                    width - 8f, neckHeight + (height * 0.08f),
                    width - 8f, neckHeight + (height * 0.2f)
                )

                // Main body right wall
                lineTo(width - 8f, height - cornerRadius)

                // Bottom right corner
                quadraticTo(
                    width - 8f, height,
                    width - cornerRadius, height
                )

                // Bottom wall
                lineTo(cornerRadius, height)

                // Bottom left corner
                quadraticTo(
                    8f, height,
                    8f, height - cornerRadius
                )

                // Main body left wall
                lineTo(8f, neckHeight + (height * 0.2f))

                // Shoulder sweep in
                cubicTo(
                    8f, neckHeight + (height * 0.08f),
                    (width - neckWidth) / 2, neckHeight + (height * 0.08f),
                    (width - neckWidth) / 2, neckHeight
                )
                
                close()
            }

            // Draw the background flask glass backing
            drawPath(
                path = bottlePath,
                color = Color(0x114FAFFF)
            )

            // 2. Clip the entire drawing so water is strictly contained inside the bottle
            clipPath(bottlePath) {
                // Calculate actual target fill height
                // Note: water fills from bottom up, so 0% fill means target Y is at bottom (height), 100% means target Y is near top
                val fillHeight = height * animatedFill.value
                val baseWaterY = height - fillHeight

                // Draw Wave 2 (Back wave, slightly lighter and offset)
                val wavePath2 = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, baseWaterY)
                    
                    val waveAmplitude = 18f
                    val waveFrequency = 0.02f
                    
                    for (x in 0..width.toInt() step 5) {
                        val y = baseWaterY + waveAmplitude * sin(x * waveFrequency + wavePhase2)
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = wavePath2,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WaterWaveLight.copy(alpha = 0.5f),
                            WaterWaveDeep.copy(alpha = 0.6f)
                        ),
                        startY = baseWaterY - 20f,
                        endY = height
                    )
                )

                // Draw Wave 1 (Front wave, deeper color, moves at a different speed)
                val wavePath1 = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, baseWaterY)
                    
                    val waveAmplitude = 22f
                    val waveFrequency = 0.015f
                    
                    for (x in 0..width.toInt() step 5) {
                        val y = baseWaterY + waveAmplitude * sin(x * waveFrequency - wavePhase1)
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = wavePath1,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WaterWaveGlow.copy(alpha = 0.85f),
                            WaterWaveDeep.copy(alpha = 0.95f)
                        ),
                        startY = baseWaterY - 30f,
                        endY = height
                    )
                )
            }
        }
    }
}
