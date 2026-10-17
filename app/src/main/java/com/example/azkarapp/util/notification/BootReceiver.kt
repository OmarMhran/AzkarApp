package com.example.azkarapp.util.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.di.ApplicationScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun prayerAlarmBootstrapper(): PrayerAlarmBootstrapper
        fun settingsRepository(): SettingsRepository
        @ApplicationScope
        fun appScope(): CoroutineScope
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        Log.d(TAG, "received $action")

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootEntryPoint::class.java
        )
        val bootstrapper = entryPoint.prayerAlarmBootstrapper()
        val settingsRepository = entryPoint.settingsRepository()

        val appScope = entryPoint.appScope()

        val pending = goAsync()
        appScope.launch {
            try {
                try {
                    bootstrapper.rescheduleAll()
                    Log.d(TAG, "prayer alarms rescheduled")
                } catch (e: Exception) {
                    Log.e(TAG, "prayer reschedule failed", e)
                }

                if (action != Intent.ACTION_TIMEZONE_CHANGED) {
                    try {
                        if (settingsRepository.zikrNotificationEnabled.first()) {
                            val interval = settingsRepository.zikrInterval.first().toLong()
                            ZikrAlarmScheduler.scheduleNextAlarm(context, interval)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "zikr reschedule failed", e)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "BootReceiver"
    }
}