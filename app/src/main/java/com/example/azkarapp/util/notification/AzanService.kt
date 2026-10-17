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
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat

class AzanService : Service() {

    companion object {
        const val ACTION_STOP_AZAN = "ACTION_STOP_AZAN"
        private const val FOREGROUND_ID = 1002
        private const val TAG = "AzanService"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private var isReceiverRegistered = false

    private val audioManager: AudioManager by lazy { getSystemService(AudioManager::class.java) }

    private val adhanAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopAzanAndRelease()
        }
    }


    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stopAzanAndRelease()
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
        if (intent?.action == ACTION_STOP_AZAN) {
            stopAzanAndRelease()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "الصلاة"
        val soundId = intent?.getStringExtra("ADHAN_SOUND_ID") ?: "default_adhan"


        val startedForeground = try {
            val notification = NotificationHelper.buildPrayerForegroundNotification(this, prayerName)
            startForeground(
                FOREGROUND_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed — تحقق من صلاحيات وإعلانات الـ Manifest", e)
            false
        }

        if (!startedForeground) {
            stopSelf()
            return START_NOT_STICKY
        }

        val skipReason = soundSkipReason()
        if (skipReason != null) {
            notifyWithoutSound(prayerName, skipReason)
            return START_NOT_STICKY
        }

        if (!requestAdhanFocus()) {
            notifyWithoutSound(prayerName, "audio focus مرفوض (غالباً مكالمة)")
            return START_NOT_STICKY
        }

        try {
            releasePlayerAsync(mediaPlayer)
            mediaPlayer = null

            val player = MediaPlayer.create(
                this,
                AdhanSounds.resId(soundId),
                adhanAttributes,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            if (player == null) {
                stopAzanAndRelease()
            } else {
                player.setOnCompletionListener { stopAzanAndRelease() }
                mediaPlayer = player
                player.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed", e)
            stopAzanAndRelease()
        }

        return START_NOT_STICKY
    }

    private fun soundSkipReason(): String? = when {
        audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL -> "الجهاز على الصامت أو الاهتزاز"
        audioManager.mode != AudioManager.MODE_NORMAL -> "مكالمة أو رنين جاري"
        else -> null
    }

    private fun requestAdhanFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(adhanAttributes)
            .setOnAudioFocusChangeListener(focusListener)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .build()

        val granted = audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (granted) focusRequest = request
        return granted
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun notifyWithoutSound(prayerName: String, reason: String) {
        Log.d(TAG, "adhan sound skipped: $reason")
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
        NotificationHelper.showPrayerNotification(this, prayerName)
        stopSelf()
    }

    private fun stopAzanAndRelease() {
        val player = mediaPlayer
        mediaPlayer = null

        try {
            player?.setVolume(0f, 0f)
            player?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        abandonFocus()

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }

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
        stopAzanAndRelease()
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