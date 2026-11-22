package com.example.azkarapp.util.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.azkarapp.MainActivity
import com.example.azkarapp.R
import com.example.azkarapp.presentation.azan.AzanActivity

object NotificationHelper {

    // v2: اتغير الـ ID عشان إعدادات القناة (الصوت) بتتثبت أول مرة وما بتتغيرش بعدها
    const val PRAYER_CHANNEL_ID = "prayer_times_channel_v2"
    private const val OLD_PRAYER_CHANNEL_ID = "prayer_times_channel"

    // Popup notification
    const val ZIKR_CHANNEL_ID = "continuous_zikr_popup"

    // Foreground Service notification
    const val ZIKR_SERVICE_CHANNEL_ID = "continuous_zikr_service"

    const val ZIKR_NOTIFICATION_ID = 1001
    const val ZIKR_SERVICE_NOTIFICATION_ID = 2001

    fun createNotificationChannels(context: Context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        // مسح القناة القديمة (اللي كان ليها صوت إشعار افتراضي)
        manager.deleteNotificationChannel(OLD_PRAYER_CHANNEL_ID)

        // Prayer channel (بدون صوت إشعار، لأن صوت الأذان بيشتغل من AzanService)
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

        // Zikr popup channel
        val zikrPopupChannel = NotificationChannel(
            ZIKR_CHANNEL_ID,
            "تنبيهات الذكر",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تنبيهات الذكر المستمر"
            enableVibration(true)
        }

        manager.createNotificationChannel(zikrPopupChannel)

        // Foreground service channel
        val zikrServiceChannel = NotificationChannel(
            ZIKR_SERVICE_CHANNEL_ID,
            "خدمة الذكر",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "خدمة الذكر المستمرة"
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(zikrServiceChannel)
    }

    fun showPrayerNotification(
        context: Context,
        prayerName: String
    ) {

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                prayerName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                PRAYER_CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_azkar_outline
                )
                .setContentTitle(
                    "حيّ على الصلاة"
                )
                .setContentText(
                    "حان الآن موعد أذان صلاة $prayerName"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setContentIntent(
                    pendingIntent
                )
                .setAutoCancel(true)
                .setOngoing(false)
                .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            prayerName.hashCode(),
            notification
        )
    }

    fun buildZikrNotification(
        context: Context,
        zikrText: String,
        count: Int
    ): Notification {

        // 🌟 لازم contentIntent عشان الضغط على الإشعار يتصرف كـ "click" فعلي
        // (وبالتالي autoCancel يشتغل ويقفلها) بدل ما يبقى بدون أي أكشن
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ZIKR_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            context,
            ZIKR_CHANNEL_ID
        )
            .setSmallIcon(
                R.drawable.ic_azkar_outline
            )
            .setContentTitle(
                "الذكر المستمر"
            )
            .setContentText(
                zikrText
            )
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .setCategory(
                NotificationCompat.CATEGORY_REMINDER
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()
    }

    fun buildZikrServiceNotification(
        context: Context
    ): Notification {

        // ⚠️ إشعار الخدمة ده "ongoing" بتصميمه (خدمة الـ foreground نفسها)، فمش المفروض
        // يختفي بالضغط عليه — ده الإشعار اللي بيفضل موجود طول ما الخدمة شغالة في الخلفية.
        // لو اللي بيفضل ظاهر عندك هو ده مش buildZikrNotification، فده السلوك الصحيح له،
        // ولازم توقف/تلغي الخدمة نفسها عشان يختفي، مش تعتمد على الضغط عليه.
        return NotificationCompat.Builder(
            context,
            ZIKR_SERVICE_CHANNEL_ID
        )
            .setSmallIcon(
                R.drawable.ic_azkar_outline
            )
            .setContentTitle(
                "الذكر المستمر"
            )
            .setContentText(
                "خدمة الذكر تعمل في الخلفية"
            )
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .setOngoing(true)
            .build()
    }

    // بناء إشعار أذان: الإيقاف بيتم بالـ swipe (أندرويد 14+) أو بزرار "إيقاف الأذان" (كل النسخ)
    fun buildPrayerForegroundNotification(
        context: Context,
        prayerName: String
    ): Notification {

        // Broadcast مباشر للـ AzanService (مسجّل جواه) عشان الإيقاف يوصل أسرع
        val stopIntent = Intent(AzanService.ACTION_STOP_AZAN).apply {
            setPackage(context.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // شاشة الأذان بملء الشاشة (زي منبه الساعة): بتفتح فوق قفل الشاشة لما الجهاز مقفول.
        // لو الجهاز شغال والمستخدم ماسكه، أندرويد بيعرضها كإشعار منبثق بدل ما يقاطعه.
        val fullScreenIntent = Intent(context, AzanActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AzanActivity.EXTRA_PRAYER_NAME, prayerName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, PRAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_azkar_outline)
            .setContentTitle("حيّ على الصلاة")
            .setContentText("حان الآن موعد أذان صلاة $prayerName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(stopPendingIntent) // swipe (أندرويد 14+)
            .addAction(0, "إيقاف الأذان", stopPendingIntent) // يشتغل على كل النسخ
            .setOngoing(false)
            // false عشان الضغط على الإشعار ما يشيلوش والصوت شغال (وده كان بيلغي الـ deleteIntent)
            .setAutoCancel(false)
            .build()
    }

    /**
     * أندرويد 14+: صلاحية "الإشعارات بملء الشاشة" مش ممنوحة تلقائياً لأي تطبيق غير المنبهات والمكالمات،
     * والمستخدم لازم يفعّلها من إعدادات التطبيق. قبل كده بتبقى ممنوحة بمجرد إعلانها في الـ Manifest.
     */
    fun canUseFullScreen(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager.canUseFullScreenIntent()
    }

    /** بيفتح شاشة إعدادات صلاحية الإشعارات بملء الشاشة للتطبيق (أندرويد 14+). */
    fun openFullScreenSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val intent = Intent(
            android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
            android.net.Uri.parse("package:${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    /**
     * إشعار "الأذان خلص": بيفضل في الشريط لحد ما المستخدم يمسحه أو يضغط عليه،
     * علشان لو ما سمعش الأذان يشوفه ويفتكر. بيستبدل إشعار الخدمة (نفس الـ id).
     */
    fun showAzanFinishedNotification(context: Context, notificationId: Int, prayerName: String) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, PRAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_azkar_outline)
            .setContentTitle("حيّ على الصلاة")
            .setContentText("أذان صلاة $prayerName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}