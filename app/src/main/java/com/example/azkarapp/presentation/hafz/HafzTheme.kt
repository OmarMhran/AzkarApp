package com.example.azkarapp.presentation.hafz

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** لون ميزة الحفظ (نيلي): أفتح في الدارك وأغمق في اللايت علشان النص يفضل مقروء. */
@Composable
fun hafzAccent(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) Color(0xFFA9AEF2) else Color(0xFF4B50A8)
}

@Composable
fun hafzTintAlpha(): Float {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) 0.16f else 0.10f
}
