package com.example.azkarapp.util.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.azkarapp.MainActivity
import com.example.azkarapp.R

object NotificationHelper {

    const val PRAYER_CHANNEL_ID = "prayer_times_channel_v2"
    const val ZIKR_CHANNEL_ID = "continuous_zikr_popup"
    const val ZIKR_NOTIFICATION_ID = 1001

    private const val OLD_PRAYER_CHANNEL_ID = "prayer_times_channel"
    private const val OLD_ZIKR_SERVICE_CHANNEL_ID = "continuous_zikr_service"

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        manager.deleteNotificationChannel(OLD_PRAYER_CHANNEL_ID)
        manager.deleteNotificationChannel(OLD_ZIKR_SERVICE_CHANNEL_ID)

        val prayerChannel = NotificationChannel(
            PRAYER_CHANNEL_ID,
            "أوقات الصلاة",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات أوقات الصلاة"
            setSound(null, null)
            enableVibration(true)
        }
        manager.createNotificationChannel(prayerChannel)

        val zikrChannel = NotificationChannel(
            ZIKR_CHANNEL_ID,
            "تنبيهات الذكر",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تنبيهات الذكر المستمر"
            enableVibration(true)
        }
        manager.createNotificationChannel(zikrChannel)
    }

    private fun openAppPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showPrayerNotification(context: Context, prayerName: String) {
        val notification = NotificationCompat.Builder(context, PRAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_azkar_outline)
            .setContentTitle("حيّ على الصلاة")
            .setContentText("حان الآن موعد أذان صلاة $prayerName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent(context, prayerName.hashCode()))
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(prayerName.hashCode(), notification)
    }

    @Suppress("UNUSED_PARAMETER")
    fun buildZikrNotification(
        context: Context,
        zikrText: String,
        count: Int
    ): Notification {
        return NotificationCompat.Builder(context, ZIKR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_azkar_outline)
            .setContentTitle("الذكر المستمر")
            .setContentText(zikrText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent(context, ZIKR_NOTIFICATION_ID))
            .setAutoCancel(true)
            .setOngoing(false)
            .build()
    }

    fun buildPrayerForegroundNotification(
        context: Context,
        prayerName: String
    ): Notification {
        val stopIntent = Intent(AzanService.ACTION_STOP_AZAN).apply {
            setPackage(context.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, PRAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_azkar_outline)
            .setContentTitle("حيّ على الصلاة")
            .setContentText("حان الآن موعد أذان صلاة $prayerName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent(context, prayerName.hashCode()))
            .setDeleteIntent(stopPendingIntent)
            .addAction(0, "إيقاف الأذان", stopPendingIntent)
            .setOngoing(false)
            .setAutoCancel(false)
            .build()
    }
}