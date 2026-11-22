package com.example.azkarapp.presentation.main_hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.azkarapp.presentation.main_hub.home_screen.HomeScreen
import com.example.azkarapp.presentation.main_hub.misbaha_screen.MisbahaScreen
import com.example.azkarapp.presentation.main_hub.prayer_times.PrayerTimesScreen
import com.example.azkarapp.presentation.main_hub.settings_screen.SettingsScreen
import com.example.azkarapp.ui.navigation.NavTransitions
import com.example.azkarapp.ui.navigation.Screen

@Composable
fun MainHub(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.PrayerTimes,
        Screen.Misbaha,
        Screen.Settings
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val colors = MaterialTheme.colorScheme
        val isDark = colors.background.luminance() < 0.5f
        val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        // الدارك: شريط أفتح من الخلفية + حد رفيع + كبسولة نشطة بلون الـ primary
        val barColor = if (isDark) {
            colors.surfaceContainerHigh
        } else {
            colors.surfaceContainerLowest
        }
        val activePillColor = if (isDark) {
            colors.primary.copy(alpha = 0.22f)
        } else {
            colors.surfaceContainerHigh
        }
        val activeTint = if (isDark) colors.primary else colors.onSurface
        val inactiveTint = if (isDark) {
            colors.onSurface.copy(alpha = 0.65f)
        } else {
            colors.onSurfaceVariant.copy(alpha = 0.7f)
        }

        var isNavVisible by remember { mutableStateOf(true) }

        val navScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // السحب للأعلى لتصفح المحتوى لأسفل -> إخفاء الشريط
                    if (available.y < -4f && isNavVisible) {
                        isNavVisible = false
                    }
                    // السحب للأسفل للعودة لأعلى -> إظهار الشريط
                    else if (available.y > 4f && !isNavVisible) {
                        isNavVisible = true
                    }
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(navScrollConnection)
                // يلتقط السحب الرأسي في أي شاشة (زي المسبحة اللي بتاخد الكليك) من غير ما يستهلكه
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        var total = 0f
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            total += event.changes.sumOf {
                                (it.position.y - it.previousPosition.y).toDouble()
                            }.toFloat()
                            if (total < -48f) {
                                isNavVisible = false
                                total = 0f
                            } else if (total > 48f) {
                                isNavVisible = true
                                total = 0f
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = colors.background
            ) { innerPadding ->
                NavHost(
                    navController = bottomNavController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(innerPadding),
                    enterTransition = NavTransitions.tabEnter,
                    exitTransition = NavTransitions.tabExit,
                    popEnterTransition = NavTransitions.tabEnter,
                    popExitTransition = NavTransitions.tabExit
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            rootNavController = rootNavController,
                            onNavigateToTab = { index ->
                                val targetScreen = items[index]
                                isNavVisible = true
                                bottomNavController.navigate(targetScreen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.PrayerTimes.route) {
                        PrayerTimesScreen(
                            onNavigateToCompass = {
                                rootNavController.navigate(Screen.Compass.route)
                            }
                        )
                    }
                    composable(Screen.Misbaha.route) { MisbahaScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }

            AnimatedVisibility(
                visible = isNavVisible,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 200)
                ) + fadeIn(animationSpec = tween(durationMillis = 160)),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 180)
                ) + fadeOut(animationSpec = tween(durationMillis = 140)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Surface(
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(50),
                    color = barColor,
                    tonalElevation = 0.dp,
                    shadowElevation = if (isDark) 8.dp else 5.dp,
                    border = if (isDark) {
                        BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.14f))
                    } else null
                ) {
                    Row(
                        modifier = Modifier
                            .height(54.dp)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { screen ->
                            val isSelected =
                                currentDestination?.hierarchy?.any { it.route == screen.route } == true

                            val pillBg by animateColorAsState(
                                targetValue = if (isSelected) activePillColor else Color.Transparent,
                                animationSpec = tween(durationMillis = 180),
                                label = "navPillBg"
                            )
                            val contentTint by animateColorAsState(
                                targetValue = if (isSelected) activeTint else inactiveTint,
                                animationSpec = tween(durationMillis = 180),
                                label = "navContentTint"
                            )
                            val interactionSource = remember { MutableInteractionSource() }

                            Box(
                                modifier = Modifier
                                    .width(56.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(pillBg)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = {
                                            bottomNavController.navigate(screen.route) {
                                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getNavIcon(screen = screen, isSelected = isSelected),
                                    contentDescription = stringResource(screen.title),
                                    tint = contentTint,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getNavIcon(screen: Screen, isSelected: Boolean): ImageVector {
    return when (screen) {
        Screen.Home -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.PrayerTimes -> if (isSelected) Icons.Filled.AccessTime else Icons.Outlined.AccessTime
        Screen.Misbaha -> Icons.Rounded.Fingerprint
        Screen.Settings -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
        else -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
    }
}