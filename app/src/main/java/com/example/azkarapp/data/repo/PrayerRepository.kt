package com.example.azkarapp.data.repo

import kotlinx.coroutines.flow.Flow

data class NextPrayerInfo(
    val name: String,
    val time: String,
    val remainingTime: String,
    val countdownFormatted: String,
    val isNextDay: Boolean
)

enum class LocationSource { GPS, CACHED, COUNTRY }

interface PrayerRepository {

    data class LocationResult(
        val lat: Double,
        val lon: Double,
        val cityName: String,
        val source: LocationSource = LocationSource.GPS,
        val timeZoneId: String? = null
    )

    val selectedCountryCode: Flow<String>
    suspend fun getSelectedCountryCode(): String
    suspend fun saveCountrySelection(countryCode: String, methodId: Int)
    suspend fun isOnboardingDone(): Boolean

    fun hasLocationPermission(): Boolean

    fun hasNotificationPermission(): Boolean
    fun isOnline(): Boolean
    fun networkAvailableFlow(): Flow<Unit>

    suspend fun getBestKnownLocation(): LocationResult?
    suspend fun resolveLocation(): LocationResult?
    suspend fun fetchLocationAndCity(): LocationResult
    suspend fun getLocationFromSelectedCountry(): LocationResult
    suspend fun hasSavedLocation(): Boolean

    suspend fun getPrayerTimes(
        location: LocationResult,
        dayOffset: Int = 0
    ): LinkedHashMap<String, String>

    suspend fun getPrayerTimes(
        lat: Double,
        lon: Double,
        timeZoneId: String? = null,
        dayOffset: Int = 0
    ): LinkedHashMap<String, String>

    fun calculateNextPrayer(prayerTimesMap: LinkedHashMap<String, String>): NextPrayerInfo

    suspend fun savePrayerNotificationState(prayerName: String, isEnabled: Boolean)
    fun getPrayerNotificationSettings(): Flow<Map<String, Boolean>>
}
