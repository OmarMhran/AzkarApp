package com.example.azkarapp.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** كل إعدادات شكل الـ Widget في كائن واحد. */
data class WidgetConfig(
    val style: WidgetStyle = WidgetStyle.COMPACT,
    val fontScale: Int = 100,
    val opacity: Int = WidgetStyle.DEFAULT_OPACITY
)

/**
 * الوسيط الوحيد بين الإعدادات والـ Widget.
 * التخزين لسه في SharedPreferences (عن طريق WidgetStyle) لأن الـ Provider بيقرأ منها،
 * لكن الشاشة مبقتش بتلمس Context ولا SharedPreferences.
 */
@Singleton
class WidgetSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _config = MutableStateFlow(read())
    val config: StateFlow<WidgetConfig> = _config.asStateFlow()

    private fun read() = WidgetConfig(
        style = WidgetStyle.get(context),
        fontScale = WidgetStyle.getFontScale(context),
        opacity = WidgetStyle.getOpacity(context)
    )

    fun setStyle(style: WidgetStyle) {
        WidgetStyle.set(context, style)
        _config.update { it.copy(style = style) }
    }

    fun setFontScale(percent: Int) {
        WidgetStyle.setFontScale(context, percent)
        _config.update { it.copy(fontScale = percent) }
    }

    fun setOpacity(percent: Int) {
        val value = percent.coerceIn(WidgetStyle.MIN_OPACITY, 100)
        WidgetStyle.setOpacity(context, value)
        _config.update { it.copy(opacity = value) }
    }
}
