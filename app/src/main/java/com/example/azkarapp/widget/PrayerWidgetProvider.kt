package com.example.azkarapp.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.res.ColorStateList
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.example.azkarapp.MainActivity
import com.example.azkarapp.R
import com.example.azkarapp.data.repo.PrayerRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class PrayerWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun prayerRepository(): PrayerRepository
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refresh(context)

    override fun onEnabled(context: Context) = refresh(context)

    override fun onDisabled(context: Context) = cancelAlarm(context)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> refresh(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.azkarapp.widget.REFRESH"
        private const val TAG = "PrayerWidget"
        private val ORDER = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        private val CELLS = listOf(
            Triple(R.id.name_fajr, R.id.time_fajr, R.id.cell_fajr),
            Triple(R.id.name_dhuhr, R.id.time_dhuhr, R.id.cell_dhuhr),
            Triple(R.id.name_asr, R.id.time_asr, R.id.cell_asr),
            Triple(R.id.name_maghrib, R.id.time_maghrib, R.id.cell_maghrib),
            Triple(R.id.name_isha, R.id.time_isha, R.id.cell_isha),
        )

        fun requestUpdate(context: Context) {
            val app = context.applicationContext
            val ids = AppWidgetManager.getInstance(app)
                .getAppWidgetIds(ComponentName(app, PrayerWidgetProvider::class.java))
            if (ids.isNotEmpty()) refresh(app)
        }

        private fun refresh(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, PrayerWidgetProvider::class.java))
            if (ids.isEmpty()) { cancelAlarm(app); return }

            ids.forEach { manager.updateAppWidget(it, build(app, null)) }

            val repo = EntryPointAccessors
                .fromApplication(app, WidgetEntryPoint::class.java).prayerRepository()
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                var nextTrigger = midnightPlusMinute()
                try {
                    val location = repo.getBestKnownLocation()
                    val times = location?.let { repo.getPrayerTimes(it) }
                    val nextIdx = times?.let { m ->
                        val name = repo.calculateNextPrayer(LinkedHashMap(m)).name
                        ORDER.indexOf(if (name == "الجمعة") "الظهر" else name)
                    } ?: -1
                    val views = build(app, times, nextIdx)
                    ids.forEach { manager.updateAppWidget(it, views) }
                    times?.let { nextTrigger = minOf(nextTrigger, nextPrayerTrigger(it)) }
                } catch (e: Exception) {
                    Log.e(TAG, "widget refresh failed", e)
                } finally {
                    scheduleAlarm(app, nextTrigger)
                }
            }
        }

        private fun build(context: Context, times: Map<String, String>?, nextIndex: Int = -1): RemoteViews {
            val style = WidgetStyle.get(context)
            val v = RemoteViews(
                context.packageName,
                when (style) {
                    WidgetStyle.REGULAR -> R.layout.widget_prayer_large
                    WidgetStyle.SQUARE -> R.layout.widget_prayer_square
                    WidgetStyle.COMPACT -> R.layout.widget_prayer
                }
            )
            val ar = Locale("ar")
            val today = LocalDate.now()
            v.setTextViewText(
                R.id.hijri_date,
                HijrahDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy هـ", ar))
            )
            v.setTextViewText(
                R.id.gregorian_date,
                today.format(
                    DateTimeFormatter.ofPattern(
                        if (style == WidgetStyle.SQUARE) "EEEE d MMMM" else "EEEE، d MMMM yyyy", ar
                    )
                )
            )

            val values = times?.let { m ->
                ORDER.map { key -> m[key] ?: if (key == "الظهر") m["الجمعة"] else null }
            }

            CELLS.forEachIndexed { i, (nameId, timeId, cellId) ->
                v.setTextViewText(timeId, values?.get(i) ?: "--:--")
                val active = i == nextIndex
                v.setInt(cellId, "setBackgroundResource",
                    if (active) R.drawable.widget_cell_active else 0)
                v.setDualTextColor(context, nameId,
                    if (active) R.color.widget_on_accent else R.color.widget_text_secondary)
                v.setDualTextColor(context, timeId,
                    if (active) R.color.widget_on_accent else R.color.widget_text_primary)
            }

            if (style == WidgetStyle.REGULAR) {
                v.setTextViewText(
                    R.id.next_label,
                    if (nextIndex >= 0) "القادمة: ${ORDER[nextIndex]}" else ""
                )
            }

            val alpha = (WidgetStyle.getOpacity(context) * 255 / 100).coerceIn(0, 255)
            val (surfaceLight, surfaceDark) = colorPair(context, R.color.widget_surface)
            v.setColorStateList(
                R.id.widget_root, "setBackgroundTintList",
                ColorStateList.valueOf(withAlpha(surfaceLight, alpha)),
                ColorStateList.valueOf(withAlpha(surfaceDark, alpha))
            )

            applyFontSizes(context, v, style)

            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            v.setOnClickPendingIntent(R.id.widget_root, open)
            return v
        }

        private fun baseSizes(style: WidgetStyle): FloatArray = when (style) {
            WidgetStyle.COMPACT -> floatArrayOf(13f, 10f, 10f, 11f, 0f)
            WidgetStyle.SQUARE -> floatArrayOf(12f, 10f, 12f, 12f, 0f)
            WidgetStyle.REGULAR -> floatArrayOf(18f, 12f, 12f, 14f, 12f)
        }

        private fun applyFontSizes(context: Context, v: RemoteViews, style: WidgetStyle) {
            val scale = WidgetStyle.getFontScale(context) / 100f
            val b = baseSizes(style)
            fun size(id: Int, base: Float) =
                v.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, base * scale)
            size(R.id.hijri_date, b[0])
            size(R.id.gregorian_date, b[1])
            CELLS.forEach { (nameId, timeId, _) -> size(nameId, b[2]); size(timeId, b[3]) }
            if (style == WidgetStyle.REGULAR) size(R.id.next_label, b[4])
        }

        private fun withAlpha(color: Int, alpha: Int) = (color and 0x00FFFFFF) or (alpha shl 24)

        private fun colorPair(context: Context, colorRes: Int): Pair<Int, Int> {
            fun forMode(nightFlag: Int): Int {
                val cfg = android.content.res.Configuration(context.resources.configuration)
                cfg.uiMode = (cfg.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv()) or nightFlag
                return context.createConfigurationContext(cfg).getColor(colorRes)
            }
            return forMode(android.content.res.Configuration.UI_MODE_NIGHT_NO) to
                forMode(android.content.res.Configuration.UI_MODE_NIGHT_YES)
        }

        private fun RemoteViews.setDualTextColor(context: Context, viewId: Int, colorRes: Int) {
            val (light, dark) = colorPair(context, colorRes)
            setColorInt(viewId, "setTextColor", light, dark)
        }

        private fun parse(time: String): Calendar? = try {
            val clean = time.trim().replace("ص", "AM").replace("م", "PM")
            val d = (try { SimpleDateFormat("hh:mm a", Locale.US).parse(clean) }
                catch (e: Exception) { SimpleDateFormat("HH:mm", Locale.US).parse(clean) }) ?: return null
            val t = Calendar.getInstance().apply { this.time = d }
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, t.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) { null }

        private fun nextPrayerTrigger(times: Map<String, String>): Long {
            val now = System.currentTimeMillis()
            return times.values.mapNotNull { parse(it)?.timeInMillis }
                .filter { it > now }.minOrNull()?.plus(60_000L) ?: Long.MAX_VALUE
        }

        private fun midnightPlusMinute(): Long = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        private fun alarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 7001,
            Intent(context, PrayerWidgetProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        private fun scheduleAlarm(context: Context, at: Long) {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            am.setAndAllowWhileIdle(AlarmManager.RTC, at, alarmIntent(context))
        }

        private fun cancelAlarm(context: Context) {
            context.getSystemService(AlarmManager::class.java)?.cancel(alarmIntent(context))
        }
    }
}
