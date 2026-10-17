package com.example.azkarapp.presentation.main_hub

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = screen.icon as Int),
                                    contentDescription = stringResource(screen.title),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(stringResource(screen.title)) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            onClick = {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
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
    }
}