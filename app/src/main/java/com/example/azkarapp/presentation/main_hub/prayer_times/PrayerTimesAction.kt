package com.example.azkarapp.presentation.main_hub.prayer_times

sealed interface PrayerTimesAction {
    data object Refresh : PrayerTimesAction

    data class ToggleNotification(val keyName: String) : PrayerTimesAction
}
