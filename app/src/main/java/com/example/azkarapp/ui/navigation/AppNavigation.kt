package com.example.azkarapp.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.azkarapp.presentation.azkar_categories.CategoriesScreen
import com.example.azkarapp.presentation.azkar_categories.CategoriesViewModel
import com.example.azkarapp.presentation.azkar_groups.AzkarGroupsScreen
import com.example.azkarapp.presentation.azkar_groups.AzkarGroupsViewModel
import com.example.azkarapp.presentation.azkar_supplications.SupplicationsScreen
import com.example.azkarapp.presentation.azkar_supplications.SupplicationsViewModel
import com.example.azkarapp.presentation.splash_screen.SplashScreen
import com.example.azkarapp.presentation.main_hub.compass_screen.QiblahCompassScreen
import com.example.azkarapp.presentation.main_hub.MainHub
import com.example.azkarapp.presentation.names_screen.NamesScreen
import com.example.azkarapp.presentation.permission_screen.PermissionScreen


@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route,
        enterTransition = NavTransitions.enter,
        exitTransition = NavTransitions.exit,
        popEnterTransition = NavTransitions.popEnter,
        popExitTransition = NavTransitions.popExit
    ) {
        composable(
            route = "splash_screen",
            exitTransition = NavTransitions.launchExit
        ) {
            SplashScreen(
                onNavigateNext = { destinationRoute ->
                    val finalRoute = if (destinationRoute == "permission_screen") {
                        Screen.Permission.route
                    } else {
                        "main_hub"
                    }

                    navController.navigate(finalRoute) {
                        // حذف الـ Splash من الـ BackStack تماماً
                        popUpTo("splash_screen") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Permission.route,
            enterTransition = NavTransitions.launchEnter,
            exitTransition = NavTransitions.launchExit
        ) { PermissionScreen(navController) }

        composable(
            route = "main_hub",
            enterTransition = NavTransitions.launchEnter
        ) { MainHub(rootNavController = navController) }

        composable(Screen.AzkarSections.route) {
            val vm: AzkarGroupsViewModel = hiltViewModel()
            AzkarGroupsScreen(
                viewModel = vm, onGroupClick = { id, name ->
                    navController.navigate("categories_screen/$id/$name")
                },
                onBackClick =  { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AzkarCategories.route,
            arguments = listOf(
                navArgument("sectionId") { type = NavType.IntType },
                navArgument("sectionName") { type = NavType.StringType }
            )
        ) {
            val vm: CategoriesViewModel = hiltViewModel()
            CategoriesScreen(
                viewModel = vm,
                onCategoryClick = { id, name ->
                    navController.navigate("supplications_screen/$id/$name")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Supplications.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) {
            val vm: SupplicationsViewModel = hiltViewModel()
            SupplicationsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }



        composable(Screen.Compass.route) {
            QiblahCompassScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.NamesScreen.route) {
            NamesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
