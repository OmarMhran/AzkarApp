package com.example.azkarapp.widget

import android.content.Context

enum class WidgetStyle(val label: String, val description: String) {
    COMPACT("مضغوط", "سطر واحد: التاريخ والمواقيت (حجم 4×1)"),
    SQUARE("عمودي", "مربع صغير: المواقيت تحت بعض (حجم 2×2)"),
    REGULAR("عادي", "أكبر وأوضح مع اسم الصلاة القادمة (حجم 4×2)");

    companion object {
        private const val PREFS = "widget_prefs"
        private const val KEY = "style"

        fun get(context: Context): WidgetStyle {
            val name = try {
                context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY, null)
            } catch (e: Exception) { null }
            return entries.firstOrNull { it.name == name } ?: COMPACT
        }

        private const val KEY_FONT = "font_scale"
        val FONT_SCALES = listOf(85 to "صغير", 100 to "وسط", 115 to "كبير")

        fun getFontScale(context: Context): Int = try {
            val v = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_FONT, 100)
            if (FONT_SCALES.any { it.first == v }) v else 100
        } catch (e: Exception) { 100 }

        fun setFontScale(context: Context, percent: Int) {
            val app = context.applicationContext
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_FONT, percent).apply()
            PrayerWidgetProvider.requestUpdate(app)
        }

        private const val KEY_OPACITY = "background_opacity"
        const val MIN_OPACITY = 20
        const val DEFAULT_OPACITY = 100

        fun getOpacity(context: Context): Int = try {
            context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_OPACITY, DEFAULT_OPACITY).coerceIn(MIN_OPACITY, 100)
        } catch (e: Exception) { DEFAULT_OPACITY }

        fun setOpacity(context: Context, percent: Int) {
            val app = context.applicationContext
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_OPACITY, percent.coerceIn(MIN_OPACITY, 100)).apply()
            PrayerWidgetProvider.requestUpdate(app)
        }

        fun set(context: Context, style: WidgetStyle) {
            val app = context.applicationContext
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, style.name).apply()
            PrayerWidgetProvider.requestUpdate(app)
        }
    }
}
