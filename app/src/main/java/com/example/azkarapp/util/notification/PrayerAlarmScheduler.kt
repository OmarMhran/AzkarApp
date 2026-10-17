package com.example.azkarapp.util.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun buildPendingIntent(prayerName: String, soundId: String): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
            putExtra("ADHAN_SOUND_ID", soundId)
        }
        return PendingIntent.getBroadcast(
            context,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun schedulePrayerAlarm(
        prayerName: String,
        timeStr: String,
        isEnabled: Boolean,
        soundId: String = "default_adhan",
        tomorrow: Boolean = false
    ) {
        val pendingIntent = buildPendingIntent(prayerName, soundId)

        if (!isEnabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val normalizedTime = timeStr.replace("ص", "AM").replace("م", "PM")
        val parsedTime = try {
            SimpleDateFormat("hh:mm a", Locale.US).parse(normalizedTime)
        } catch (e: Exception) {
            null
        } ?: return

        val now = Calendar.getInstance()
        val alarmCalendar = Calendar.getInstance().apply {
            time = parsedTime
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (tomorrow || before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        setAlarm(alarmCalendar.timeInMillis, pendingIntent)
    }

    fun scheduleAt(prayerName: String, soundId: String, triggerAtMillis: Long) {
        setAlarm(triggerAtMillis, buildPendingIntent(prayerName, soundId))
    }

    fun hasPassedToday(timeStr: String): Boolean {
        val normalized = timeStr.replace("ص", "AM").replace("م", "PM")
        val parsed = try {
            SimpleDateFormat("hh:mm a", Locale.US).parse(normalized)
        } catch (e: Exception) {
            null
        } ?: return false

        val now = Calendar.getInstance()
        val prayerCal = Calendar.getInstance().apply {
            time = parsed
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return prayerCal.before(now)
    }
}