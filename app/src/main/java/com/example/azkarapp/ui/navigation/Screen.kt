package com.example.azkarapp.ui.navigation


import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List // استخدمنا النسخة العادية والنظيفة
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.azkarapp.R

sealed class Screen(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int? = null
) {
    object SplashScreen : Screen("splash_screen", R.string.app_name)
    object Permission : Screen("permission_screen", R.string.app_name)

    object Home : Screen("main_hub", R.string.home, R.drawable.ic_azkar)
    object Misbaha : Screen("misbaha_screen", R.string.misbaha, R.drawable.ic_misbaha)
    object PrayerTimes : Screen("prayer_times_screen", R.string.prayer_times, R.drawable.ic_prayer)
    object Settings : Screen("settings_screen", R.string.settings, R.drawable.ic_settings)

    object AzkarSections : Screen("azkar_sections_screen", R.string.azkar_title)
    object AzkarCategories : Screen("categories_screen/{sectionId}/{sectionName}", R.string.categories_title)
    object Supplications : Screen("supplications_screen/{categoryId}/{categoryName}", R.string.azkar_title)
    object Compass : Screen("compass_screen", R.string.compass)
    object NamesScreen : Screen("names_screen",R.string.NamesScreen)
}