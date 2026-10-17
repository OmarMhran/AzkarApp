package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.SettingsDataStore
import com.example.azkarapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {

    override val fontSize: Flow<Float> = settingsDataStore.fontSizeFlow
    override val adhanSound: Flow<String> = settingsDataStore.adhanSoundFlow
    override val zikrNotificationEnabled: Flow<Boolean> = settingsDataStore.zikrNotificationEnabledFlow
    override val isVibrationEnabled: Flow<Boolean> = settingsDataStore.isVibrationEnabledFlow
    override val zikrInterval: Flow<Int> = settingsDataStore.zikrIntervalFlow
    override val themeMode: Flow<ThemeMode> = settingsDataStore.themeModeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsDataStore.saveThemeMode(mode)
    }

    override suspend fun setZikrInterval(minutes: Int) = settingsDataStore.setZikrInterval(minutes)
    override suspend fun saveFontSize(size: Float) = settingsDataStore.saveFontSize(size)
    override suspend fun setVibrationEnabled(enabled: Boolean) = settingsDataStore.saveVibrationSetting(enabled)
    override suspend fun setAdhanSound(soundId: String) = settingsDataStore.saveAdhanSound(soundId)
    override suspend fun setZikrNotificationEnabled(enabled: Boolean) =
        settingsDataStore.saveZikrNotificationSetting(enabled)
}