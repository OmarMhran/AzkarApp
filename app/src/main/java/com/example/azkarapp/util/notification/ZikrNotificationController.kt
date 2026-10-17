package com.example.azkarapp.util.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZikrNotificationController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule(intervalMinutes: Int) {
        ZikrAlarmScheduler.scheduleNextAlarm(context, intervalMinutes.toLong())
    }

    fun cancel() {
        ZikrAlarmScheduler.cancelAlarm(context)
        NotificationManagerCompat.from(context).cancel(NotificationHelper.ZIKR_NOTIFICATION_ID)
    }
}
