package com.example.azkarapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.azkarapp.domain.model.ThemeMode
import com.example.azkarapp.di.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    @SettingsStore private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val VIBRATION_ON_FINISH = booleanPreferencesKey("vibrate_on_finish")
        val ADHAN_SOUND = stringPreferencesKey("adhan_sound")
        val ZIKR_NOTIFICATION_ENABLED = booleanPreferencesKey("zikr_notification_enabled")
        val ZIKR_INTERVAL = intPreferencesKey("zikr_notification_interval")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val fontSizeFlow: Flow<Float> = dataStore.data
        .map { it[PreferencesKeys.FONT_SIZE] ?: 18f }

    val isVibrationEnabledFlow: Flow<Boolean> = dataStore.data
        .map { it[PreferencesKeys.VIBRATION_ON_FINISH] ?: true }

    val adhanSoundFlow: Flow<String> = dataStore.data
        .map { it[PreferencesKeys.ADHAN_SOUND] ?: "default_adhan" }

    val zikrNotificationEnabledFlow: Flow<Boolean> = dataStore.data
        .map { it[PreferencesKeys.ZIKR_NOTIFICATION_ENABLED] ?: false }

    val zikrIntervalFlow: Flow<Int> = dataStore.data
        .map { it[PreferencesKeys.ZIKR_INTERVAL] ?: 30 }

    val themeModeFlow: Flow<ThemeMode> = dataStore.data
        .map { ThemeMode.fromName(it[PreferencesKeys.THEME_MODE]) }

    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun saveFontSize(newSize: Float) {
        dataStore.edit { it[PreferencesKeys.FONT_SIZE] = newSize }
    }

    suspend fun saveVibrationSetting(isEnabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.VIBRATION_ON_FINISH] = isEnabled }
    }

    suspend fun saveAdhanSound(soundName: String) {
        dataStore.edit { it[PreferencesKeys.ADHAN_SOUND] = soundName }
    }

    suspend fun saveZikrNotificationSetting(isEnabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.ZIKR_NOTIFICATION_ENABLED] = isEnabled }
    }

    suspend fun setZikrInterval(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.ZIKR_INTERVAL] = minutes }
    }
}