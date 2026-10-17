package com.example.azkarapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGreenDark,
    onPrimary = Color(0xFF003919),
    primaryContainer = IslamicGreenDarkContainer,
    onPrimaryContainer = Color(0xFFB9F6CA),

    secondary = IslamicGoldDark,
    onSecondary = Color(0xFF3E2D00),
    secondaryContainer = Color(0xFF574300),
    onSecondaryContainer = Color(0xFFFFDF9E),

    background = BackgroundDark,
    onBackground = Color(0xFFE2E2E6),

    surface = SurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF242824),
    onSurfaceVariant = Color(0xFFC2C9C2),

    surfaceContainer = Color(0xFF1B1E1B),
    surfaceContainerHigh = Color(0xFF252925),
    surfaceContainerHighest = Color(0xFF303430)
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreenLight,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,

    secondary = IslamicGoldLight,
    onSecondary = Color.White,

    background = BackgroundLight,
    onBackground = Color(0xFF191C19),

    surface = SurfaceLight,
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDEE5D9),
    onSurfaceVariant = Color(0xFF424940)
)

@Composable
fun AzkarAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(colorScheme.background.toArgb()))
            window.statusBarColor = colorScheme.surface.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                content()
            }
        }
    )
}