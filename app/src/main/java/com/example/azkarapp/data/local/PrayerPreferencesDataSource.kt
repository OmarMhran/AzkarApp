package com.example.azkarapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.azkarapp.di.PrayerDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class LocationCache(val lat: Double, val lon: Double, val cityName: String)

@Singleton
class PrayerPreferencesDataSource @Inject constructor(
    @PrayerDataStore private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_LAT = doublePreferencesKey("cached_lat")
        private val KEY_LON = doublePreferencesKey("cached_lon")
        private val KEY_CITY = stringPreferencesKey("cached_city")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_CALCULATION_METHOD = intPreferencesKey("calculation_method")
        private val KEY_SELECTED_COUNTRY_CODE = stringPreferencesKey("selected_country_code")
    }

    val cachedLocationFlow: Flow<LocationCache> = dataStore.data.map { prefs ->
        LocationCache(
            lat = prefs[KEY_LAT] ?: 30.0444,
            lon = prefs[KEY_LON] ?: 31.2357,
            cityName = prefs[KEY_CITY] ?: "القاهرة"
        )
    }

    val savedLocationFlow: Flow<LocationCache?> = dataStore.data.map { prefs ->
        val lat = prefs[KEY_LAT]
        val lon = prefs[KEY_LON]
        if (lat != null && lon != null) {
            LocationCache(lat, lon, prefs[KEY_CITY] ?: "")
        } else null
    }

    suspend fun getSavedLocation(): LocationCache? = savedLocationFlow.first()

    suspend fun saveLocationToCache(lat: Double, lon: Double, city: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAT] = lat
            prefs[KEY_LON] = lon
            prefs[KEY_CITY] = city
        }
    }

    suspend fun savePrayerNotificationState(prayerName: String, isEnabled: Boolean) {
        val key = booleanPreferencesKey("notif_$prayerName")
        dataStore.edit { prefs ->
            prefs[key] = isEnabled
        }
    }

    val prayerNotificationsFlow: Flow<Map<String, Boolean>> = dataStore.data.map { prefs ->
        mapOf(
            "الفجر" to (prefs[booleanPreferencesKey("notif_الفجر")] ?: true),
            "الشروق" to (prefs[booleanPreferencesKey("notif_الشروق")] ?: false),
            "الظهر" to (prefs[booleanPreferencesKey("notif_الظهر")] ?: true),
            "العصر" to (prefs[booleanPreferencesKey("notif_العصر")] ?: true),
            "المغرب" to (prefs[booleanPreferencesKey("notif_المغرب")] ?: true),
            "العشاء" to (prefs[booleanPreferencesKey("notif_العشاء")] ?: true)
        )
    }

    suspend fun hasSavedLocation(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[KEY_LAT] != null && prefs[KEY_LON] != null
    }

    val calculationMethodFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_CALCULATION_METHOD] ?: 4
    }

    suspend fun saveCalculationMethod(methodId: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_CALCULATION_METHOD] = methodId
        }
    }

    val selectedCountryCodeFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_COUNTRY_CODE] ?: "EG"
    }

    suspend fun saveSelectedCountryCode(countryCode: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_COUNTRY_CODE] = countryCode
        }
    }

    suspend fun saveCountrySelection(countryCode: String, methodId: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_COUNTRY_CODE] = countryCode
            prefs[KEY_CALCULATION_METHOD] = methodId
            prefs[KEY_ONBOARDING_DONE] = true

        }
    }

    suspend fun isOnboardingDone(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[KEY_ONBOARDING_DONE] == true || prefs[KEY_SELECTED_COUNTRY_CODE] != null
    }
}