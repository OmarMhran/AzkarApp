package com.example.azkarapp.util.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock


object ZikrAlarmScheduler {

    private const val REQUEST_CODE = 5001

    fun scheduleNextAlarm(
        context: Context,
        intervalMinutes: Long
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ZikrAlarmReceiver::class.java).apply {
            action = "com.example.azkarapp.ACTION_ZIKR_ALARM"
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val triggerTime =
            SystemClock.elapsedRealtime() +
                    (intervalMinutes.coerceAtLeast(1L) * 60_000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ZikrAlarmReceiver::class.java).apply {
            action = "com.example.azkarapp.ACTION_ZIKR_ALARM"
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun ensureScheduled(context: Context, intervalMinutes: Long) {
        val intent = Intent(context, ZikrAlarmReceiver::class.java).apply {
            action = "com.example.azkarapp.ACTION_ZIKR_ALARM"
        }
        val existing = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existing == null) scheduleNextAlarm(context, intervalMinutes)
    }
}