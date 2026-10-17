package com.example.azkarapp

import android.app.Application
import android.util.Log
import com.example.azkarapp.di.ApplicationScope
import com.example.azkarapp.domain.repository.AzkarRepository
import com.example.azkarapp.util.notification.NotificationHelper
import com.example.azkarapp.util.notification.PrayerAlarmBootstrapper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AzkarApplication : Application() {

    @Inject
    @field:ApplicationScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var azkarRepository: AzkarRepository
    @Inject
    lateinit var prayerAlarmBootstrapper: PrayerAlarmBootstrapper

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createNotificationChannels(this)

        appScope.launch {
            azkarRepository.populateDatabaseIfNeeded()
        }

        appScope.launch {
            try {
                azkarRepository.populateDatabaseIfNeeded()
                prayerAlarmBootstrapper.rescheduleAll()
                prayerAlarmBootstrapper.ensureZikrScheduled()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AzkarApplication", "seeding failed", e)
            }
        }
    }
}