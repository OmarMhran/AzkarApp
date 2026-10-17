package com.example.azkarapp.util.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AdhanPreviewPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var player: MediaPlayer? = null

    private val _playingSoundId = MutableStateFlow<String?>(null)
    val playingSoundId: StateFlow<String?> = _playingSoundId.asStateFlow()

    fun toggle(soundId: String) {
        if (_playingSoundId.value == soundId) {
            stop()
        } else {
            start(soundId)
        }
    }

    private fun start(soundId: String) {
        stop()

        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val newPlayer = MediaPlayer.create(
                context,
                AdhanSounds.resId(soundId),
                attrs,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            ) ?: return

            newPlayer.setOnCompletionListener { finished ->
                if (player === finished) stop()
            }

            player = newPlayer
            _playingSoundId.value = soundId
            newPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun stop() {
        val current = player
        player = null
        _playingSoundId.value = null

        if (current == null) return

        try {
            current.setVolume(0f, 0f)
            current.pause()
        } catch (_: Exception) {
        }
        Thread {
            try {
                current.stop()
            } catch (_: Exception) {
            }
            try {
                current.release()
            } catch (_: Exception) {
            }
        }.start()
    }
}