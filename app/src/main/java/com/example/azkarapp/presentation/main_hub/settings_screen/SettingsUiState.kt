package com.example.azkarapp.presentation.main_hub.settings_screen

import com.example.azkarapp.domain.model.ThemeMode
import com.example.azkarapp.widget.WidgetConfig

data class AdhanSoundItem(val id: String, val name: String)
data class ZikrIntervalItem(val minutes: Int, val name: String)

object SettingsOptions {
    val adhanSounds = listOf(
        AdhanSoundItem("default_adhan", "أذان مكة المكرمة (الافتراضي)"),
        AdhanSoundItem("madina_adhan", "أذان المدينة المنورة"),
        AdhanSoundItem("egypt_adhan", "الأذان المصري المعتمد")
    )

    val zikrIntervals = listOf(
        ZikrIntervalItem(15, "كل 15 دقيقة"),
        ZikrIntervalItem(30, "كل 30 دقيقة"),
        ZikrIntervalItem(60, "كل ساعة"),
        ZikrIntervalItem(120, "كل ساعتين")
    )
}

data class SettingsUiState(
    val fontSize: Float = 18f,
    val isVibrationEnabled: Boolean = true,
    val adhanSoundId: String = "default_adhan",
    val isZikrNotificationEnabled: Boolean = false,
    val zikrIntervalMinutes: Int = 30,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val widget: WidgetConfig = WidgetConfig(),
    val previewingSoundId: String? = null
) {
    val adhanName: String
        get() = SettingsOptions.adhanSounds.find { it.id == adhanSoundId }?.name
            ?: "أذان مكة المكرمة"

    val zikrIntervalName: String
        get() = SettingsOptions.zikrIntervals.find { it.minutes == zikrIntervalMinutes }?.name
            ?: "كل 30 دقيقة"
}
