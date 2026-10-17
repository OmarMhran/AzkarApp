package com.example.azkarapp.util.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.di.ApplicationScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun prayerRepository(): PrayerRepository
        fun prayerAlarmScheduler(): PrayerAlarmScheduler
        @ApplicationScope
        fun appScope(): CoroutineScope
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: return
        val soundId = intent.getStringExtra("ADHAN_SOUND_ID") ?: "default_adhan"

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val prayerRepository = entryPoint.prayerRepository()
        val scheduler = entryPoint.prayerAlarmScheduler()
        val appScope = entryPoint.appScope()

        scheduler.scheduleAt(
            prayerName, soundId,
            System.currentTimeMillis() + 24 * 60 * 60 * 1000L
        )

        val serviceIntent = Intent(context, AzanService::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
            putExtra("ADHAN_SOUND_ID", soundId)
        }
        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e("PrayerAlarmReceiver", "cannot start AzanService", e)
            NotificationHelper.showPrayerNotification(context, prayerName)
        }

        val pending = goAsync()
        appScope.launch {
            try {
                val location = prayerRepository.getBestKnownLocation()
                val tomorrowTime = location?.let {
                    prayerRepository.getPrayerTimes(it, dayOffset = 1)[prayerName]
                }
                if (tomorrowTime != null) {
                    scheduler.schedulePrayerAlarm(
                        prayerName, tomorrowTime, true, soundId, tomorrow = true
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pending.finish()
            }
        }
    }
}