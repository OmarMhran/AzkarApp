package com.example.azkarapp.util.notification

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.azkarapp.R

class AzanService : Service() {

    companion object {
        const val ACTION_STOP_AZAN = "ACTION_STOP_AZAN"

        /** بيتبعت (جوه التطبيق بس) لما الأذان يخلص أو يتوقف، علشان شاشة الأذان تتقفل أو تتحدّث. */
        const val ACTION_AZAN_ENDED = "ACTION_AZAN_ENDED"
        private const val FOREGROUND_ID = 1002
        private const val TAG = "AzanService"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isReceiverRegistered = false
    private var currentPrayerName: String = "الصلاة"
    private var focusRequest: AudioFocusRequest? = null

    // بيستقبل أمر الإيقاف من زرار الإشعار أو الـ swipe (deleteIntent) بشكل مباشر
    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // إيقاف يدوي (زرار أو swipe): المستخدم سمع الأذان أو مش عايزه، فنشيل الإشعار.
            stopAzanAndRelease(keepNotification = false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ContextCompat.registerReceiver(
            this,
            stopReceiver,
            IntentFilter(ACTION_STOP_AZAN),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isReceiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // دعم الإيقاف عن طريق startService كمان (للتوافق)
        if (intent?.action == ACTION_STOP_AZAN) {
            stopAzanAndRelease(keepNotification = false)
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "الصلاة"
        currentPrayerName = prayerName
        val soundId = intent?.getStringExtra("ADHAN_SOUND_ID") ?: "default_adhan"

        // 1. بدء الخدمة في المقدمة
        // ⚠️ لازم تكون معلنة في الـ Manifest: FOREGROUND_SERVICE_MEDIA_PLAYBACK permission +
        // android:foregroundServiceType="mediaPlayback" على الـ <service>، وإلا startForeground()
        // بترمي Exception من أندرويد 14 وتكرش البرنامج كله. بنلفها بـ try/catch عشان لو حصل
        // خطأ (جهاز/Manifest ناقص) نوقف الخدمة بهدوء بدل ما نكرش، ونضمن إن أي مسار بيرجع بعد
        // فشل هنا بيوقف الخدمة برضو (مفيش صوت هيتشغل من غير foreground notification شغالة).
        val startedForeground = try {
            val notification = NotificationHelper.buildPrayerForegroundNotification(this, prayerName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    FOREGROUND_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(FOREGROUND_ID, notification)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed — تحقق من صلاحيات وإعلانات الـ Manifest", e)
            false
        }

        if (!startedForeground) {
            stopSelf()
            return START_NOT_STICKY
        }

        // 2. تشغيل ملف الصوت المختار
        val resId = AdhanSounds.resId(soundId)

        try {
            // لو في أذان شغال قبل كده نفضّيه الأول
            releasePlayerAsync(mediaPlayer)
            mediaPlayer = null

            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // نطلب audio focus مؤقت: الفيديو أو الموسيقى (يوتيوب، سبوتيفاي...) بيقفوا لحد ما الأذان يخلص
            // وبعدين بيكمّلوا لوحدهم. ده طلب مش أمر: تطبيق بيتجاهل الـ focus ممكن يفضل شغال.
            // الأذان بيشتغل في كل الأحوال حتى لو الطلب ما اتقبلش.
            requestFocus(attrs)

            val player = MediaPlayer.create(
                this,
                resId,
                attrs,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            if (player == null) {
                stopAzanAndRelease(keepNotification = true)
            } else {
                // الأذان خلص لوحده: الإشعار يفضل موجود علشان لو المستخدم ما سمعوش يفتكره.
                player.setOnCompletionListener { stopAzanAndRelease(keepNotification = true) }
                mediaPlayer = player
                player.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed", e)
            stopAzanAndRelease(keepNotification = true)
        }

        return START_NOT_STICKY
    }

    private fun requestFocus(attrs: AudioAttributes) {
        try {
            val audioManager = getSystemService(AudioManager::class.java) ?: return
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { }
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } catch (e: Exception) {
            Log.e(TAG, "audio focus request failed", e)
        }
    }

    private fun abandonFocus() {
        try {
            val request = focusRequest ?: return
            focusRequest = null
            getSystemService(AudioManager::class.java)?.abandonAudioFocusRequest(request)
        } catch (_: Exception) {
        }
    }

    private fun stopAzanAndRelease(keepNotification: Boolean = false) {
        val player = mediaPlayer
        mediaPlayer = null

        abandonFocus()

        try {
            sendBroadcast(Intent(ACTION_AZAN_ENDED).setPackage(packageName))
        } catch (_: Exception) {
        }

        // كتم فوري عشان المستخدم ما يحسش بأي تأخير
        try {
            player?.setVolume(0f, 0f)
            player?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (keepNotification) {
            // نفصل الإشعار عن الخدمة (فيفضل بعد ما الخدمة تقف) ونستبدله بإشعار عادي
            // يتمسح بالـ swipe أو بالضغط عليه.
            try {
                stopForeground(STOP_FOREGROUND_DETACH)
                NotificationHelper.showAzanFinishedNotification(this, FOREGROUND_ID, currentPrayerName)
            } catch (e: Exception) {
                Log.e(TAG, "keeping notification failed", e)
            }
        } else {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (_: Exception) {
            }
        }

        // التنضيف الثقيل بعيد عن الـ main thread
        releasePlayerAsync(player)

        stopSelf()
    }

    private fun releasePlayerAsync(player: MediaPlayer?) {
        if (player == null) return
        Thread {
            try {
                player.stop()
            } catch (_: Exception) {
            }
            try {
                player.release()
            } catch (_: Exception) {
            }
        }.start()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopAzanAndRelease(keepNotification = false)
    }

    override fun onDestroy() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(stopReceiver)
            } catch (_: Exception) {
            }
            isReceiverRegistered = false
        }
        abandonFocus()
        // لو لسه في player (مثلاً النظام قتل الخدمة) نفضّيه
        val player = mediaPlayer
        mediaPlayer = null
        try {
            player?.setVolume(0f, 0f)
        } catch (_: Exception) {
        }
        releasePlayerAsync(player)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}