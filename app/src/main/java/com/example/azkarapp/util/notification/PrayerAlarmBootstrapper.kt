package com.example.azkarapp.util.notification

import android.content.Context
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.data.repo.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PrayerAlarmBootstrapper @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val settingsRepository: SettingsRepository,
    private val scheduler: PrayerAlarmScheduler,
    @ApplicationContext private val context: Context
) {
    suspend fun rescheduleAll() {
        val location = prayerRepository.getBestKnownLocation() ?: return
        val settings = prayerRepository.getPrayerNotificationSettings().first()
        val soundId = settingsRepository.adhanSound.first()

        val today = prayerRepository.getPrayerTimes(location)
        val tomorrow = prayerRepository.getPrayerTimes(location, dayOffset = 1)

        today.forEach { (name, timeStr) ->
            val enabled = settings[name] ?: true
            val tomorrowStr = tomorrow[name]

            if (tomorrowStr != null && scheduler.hasPassedToday(timeStr)) {
                scheduler.schedulePrayerAlarm(name, tomorrowStr, enabled, soundId, tomorrow = true)
            } else {
                scheduler.schedulePrayerAlarm(name, timeStr, enabled, soundId)
            }
        }
    }

    suspend fun ensureZikrScheduled() {
        if (settingsRepository.zikrNotificationEnabled.first()) {
            val interval = settingsRepository.zikrInterval.first().toLong()
            ZikrAlarmScheduler.ensureScheduled(context, interval)
        }
    }
}