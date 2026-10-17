package com.example.azkarapp.presentation.main_hub.settings_screen

import com.example.azkarapp.domain.model.ThemeMode
import com.example.azkarapp.widget.WidgetStyle

sealed interface SettingsAction {
    data class FontSizeChanged(val size: Float) : SettingsAction
    data class VibrationToggled(val enabled: Boolean) : SettingsAction
    data class ThemeSelected(val mode: ThemeMode) : SettingsAction

    data class AdhanSelected(val soundId: String) : SettingsAction
    data class AdhanPreviewToggled(val soundId: String) : SettingsAction
    data object AdhanPreviewStopped : SettingsAction

    data class ZikrNotificationToggled(val enabled: Boolean) : SettingsAction
    data class ZikrIntervalSelected(val minutes: Int) : SettingsAction

    data class WidgetStyleSelected(val style: WidgetStyle) : SettingsAction
    data class WidgetFontScaleSelected(val percent: Int) : SettingsAction
    data class WidgetOpacityChanged(val percent: Int) : SettingsAction
}
