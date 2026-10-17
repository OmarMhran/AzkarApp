package com.example.azkarapp.data.repo

import com.example.azkarapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val fontSize: Flow<Float>
    val adhanSound: Flow<String>
    val zikrNotificationEnabled: Flow<Boolean>
    val isVibrationEnabled: Flow<Boolean>
    val zikrInterval: Flow<Int>

    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setZikrInterval(minutes: Int)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun saveFontSize(size: Float)
    suspend fun setAdhanSound(soundId: String)
    suspend fun setZikrNotificationEnabled(enabled: Boolean)
}