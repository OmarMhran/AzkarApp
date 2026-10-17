package com.example.azkarapp.domain.model

enum class ThemeMode(val label: String) {
    SYSTEM("حسب الجهاز"),
    LIGHT("فاتح"),
    DARK("داكن");

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}