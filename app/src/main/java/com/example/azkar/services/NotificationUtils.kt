package com.example.azkar.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.azkar.R
import com.example.azkar.ui.home.HomeActivity
import com.example.azkar.util.Constants


class NotificationUtils {

    private fun NotificationUtils() {
        this.NotificationUtils()
    }

    private val NOTIFICATION_ID = 9000
    private val PENDING_INTENT_ID = 9001
    private val NOTIFICATION_CHANNEL_ID = Constants.PRAYER_CHANNEL_ID
    private var mNotificationBuilder: NotificationCompat.Builder? = null
    private var mNotificationManager: NotificationManager? = null
    private var isNotificationNew = true


    fun showNotification(context: Context, title: String, body: String) {
        if (!isNotificationNew) {
            updateNotification(title, body)
            return
        }

        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel on Android 8.0 and Above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Prepare name, description, and importance (PRIORITY) for our channel
            val name = "Awesome Notification Channel"
            val description = "This channel shows only awesome notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.PRAYER_CHANNEL_ID, name, importance)
            mNotificationManager!!.createNotificationChannel(channel)
            channel.description = description
        }

        // Create the Notification with the Builder
        mNotificationBuilder = NotificationCompat.Builder(context, Constants.PRAYER_CHANNEL_ID)

            // Put all notifications under one as a group
            .setGroup(NotificationCompat.CATEGORY_REMINDER)
            .setGroupSummary(true)
            .setSmallIcon(R.drawable.ic_mosque_app)
            .setContentTitle(title)
            .setContentText(body)
            // Show notification on lock screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Actions
            .addAction(actionClearNotifications(context))
            // Close the notification when user tap on it
            .setAutoCancel(true);
        // Notification Intrusive (PRIORITY) on Android 7.1 and Lower.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mNotificationBuilder!!.priority = NotificationCompat.PRIORITY_MAX
        }

        // Build it
        mNotificationManager!!.notify(NOTIFICATION_ID, mNotificationBuilder!!.build());
        isNotificationNew = false


    }

    private fun updateNotification(title: String, body: String) {
        mNotificationBuilder!!.setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setOnlyAlertOnce(true)

        mNotificationManager!!.notify(NOTIFICATION_ID, mNotificationBuilder!!.build())
    }

    private fun contentIntent(context: Context): PendingIntent? {
        val startActivityIntent = Intent(context, HomeActivity::class.java)
        return PendingIntent.getActivity(
            context,
            PENDING_INTENT_ID,
            startActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun clearNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancelAll()

    }

    private fun actionClearNotifications(context: Context): NotificationCompat.Action {
        val intent = Intent(context, MyService::class.java)
        intent.action = Constants.ACTION_STOP_SERVICE

        val pendingIntent = PendingIntent.getService(
            context,
            PENDING_INTENT_ID,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val action: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_cancel,
            "إغلاق",
            pendingIntent
        )



        return action
    }
}