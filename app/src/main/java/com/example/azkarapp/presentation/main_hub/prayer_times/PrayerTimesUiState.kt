package com.example.azkarapp.presentation.main_hub.prayer_times

import androidx.compose.runtime.Immutable
import com.example.azkarapp.data.repo.LocationSource

@Immutable
data class PrayerUiModel(
    val name: String,
    val realKeyName: String,
    val time: String,
    val isNext: Boolean = false,
    val isNotificationEnabled: Boolean = true
)

@Immutable
data class PrayerTimesUiState(
    val isLoading: Boolean = true,
    val isPermissionDenied: Boolean = false,
    val hasLocationPermission: Boolean = true,
    val hasNotificationPermission: Boolean = true,
    val prayersList: List<PrayerUiModel> = emptyList(),
    val nextPrayerName: String = "",
    val cityName: String = "جاري تحديد الموقع...",
    val hijriDate: String = "",
    val errorMessage: String? = null,
    val locationSource: LocationSource? = null
)
