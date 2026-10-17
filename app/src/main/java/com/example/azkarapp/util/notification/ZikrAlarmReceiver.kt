package com.example.azkarapp.util.notification


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.azkarapp.data.repo.MisbahaRepository
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ZikrAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var misbahaRepository: MisbahaRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    @field:ApplicationScope
    lateinit var scope: CoroutineScope

    private val defaultAzkar = listOf(
        "سبحان الله",
        "الحمد لله",
        "لا إله إلا الله",
        "الله أكبر",
        "استغفر الله",
        "لا حول ولا قوة إلا بالله"
    )

    @SuppressLint("MissingPermission")
    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val pendingResult = goAsync()

        scope.launch {
            try {
                if (!settingsRepository.zikrNotificationEnabled.first()) return@launch

                val interval = settingsRepository.zikrInterval.first().toLong()
                ZikrAlarmScheduler.scheduleNextAlarm(context, interval)

                val customAzkar = misbahaRepository.customAzkar.first().map { it.name }
                val zikr = (defaultAzkar + customAzkar).distinct().random()
                val count = misbahaRepository.currentCount.first()

                NotificationManagerCompat.from(context).notify(
                    NotificationHelper.ZIKR_NOTIFICATION_ID,
                    NotificationHelper.buildZikrNotification(context, zikr, count)
                )
            } catch (e: Exception) {
                Log.e("ZikrAlarmReceiver", "zikr alarm failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}