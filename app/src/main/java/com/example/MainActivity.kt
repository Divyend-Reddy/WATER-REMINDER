package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.WaterFlowTheme
import com.example.ui.theme.WaterWaveDeep
import com.example.ui.theme.WaterWaveGlow
import com.example.ui.theme.WaterWaveLight
import com.example.ui.theme.SleekHeaderStart
import com.example.ui.theme.SleekHeaderEnd
import com.example.ui.viewmodel.WaterViewModel
import kotlinx.coroutines.delay

enum class AppScreen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.LocalDrink, Icons.Outlined.LocalDrink),
    STATS("stats", "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    ACHIEVEMENTS("achievements", "Badges", Icons.Filled.WorkspacePremium, Icons.Outlined.WorkspacePremium),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WaterViewModel = viewModel()
            
            // Observe settings to configure Dark / Light theme dynamically
            val darkModeSetting by viewModel.darkModeSetting.collectAsState()
            val isDarkTheme = when (darkModeSetting) {
                1 -> false // Forced Light
                2 -> true  // Forced Dark
                else -> isSystemInDarkTheme() // System default
            }

            WaterFlowTheme(darkTheme = isDarkTheme) {
                var showSplash by remember { mutableStateOf(true) }

                // Crossfade between Splash Screen and Dashboard
                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    },
                    label = "SplashTransition"
                ) { isSplashActive ->
                    if (isSplashActive) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        AppDashboard(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scaleAnim = remember { Animatable(0.5f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run splash animations
        scaleAnim.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaAnim.animateTo(1f, animationSpec = tween(600))
        delay(1200) // Stay for 1.2s
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF04060B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radiant glowing droplet
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim.value)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(WaterWaveGlow.copy(alpha = 0.25f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Filled.LocalDrink,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(WaterWaveGlow),
                    modifier = Modifier
                        .size(64.dp)
                        .scale(scaleAnim.value)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Text Label
            Text(
                text = "WaterFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("splash_title")
            )

            Text(
                text = "Elegant Hydration Habits",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = WaterWaveLight.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AppDashboard(viewModel: WaterViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    
    // Check screen size classification for adaptive layout
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WELCOME BACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "WaterFlow",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SleekHeaderStart, SleekHeaderEnd)
                                )
                            )
                        )
                    }

                    IconButton(
                        onClick = { currentScreen = AppScreen.SETTINGS },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = WaterWaveLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp
                ) {
                    AppScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WaterWaveDeep,
                                selectedTextColor = WaterWaveDeep,
                                indicatorColor = WaterWaveLight.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Side navigation rail for tablets / wide screens
            if (isTablet) {
                NavigationRail(
                    modifier = Modifier.testTag("side_nav_rail"),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    header = {
                        Icon(
                            imageVector = Icons.Filled.LocalDrink,
                            contentDescription = "WaterFlow logo",
                            tint = WaterWaveDeep,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(vertical = 8.dp)
                        )
                    }
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    AppScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = WaterWaveDeep,
                                selectedTextColor = WaterWaveDeep,
                                indicatorColor = WaterWaveLight.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag("nav_rail_${screen.route}")
                        )
                    }
                }
            }

            // Screen container with crossfade animation transition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "ScreenCrossfade"
                ) { targetScreen ->
                    when (targetScreen) {
                        AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                        AppScreen.STATS -> StatsScreen(viewModel = viewModel)
                        AppScreen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
                        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
