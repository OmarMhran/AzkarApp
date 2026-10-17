package com.example.azkarapp.presentation.main_hub.settings_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.util.notification.AdhanPreviewPlayer
import com.example.azkarapp.util.notification.PrayerAlarmBootstrapper
import com.example.azkarapp.util.notification.ZikrNotificationController
import com.example.azkarapp.widget.WidgetSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val widgetSettings: WidgetSettingsRepository,
    private val zikrNotifications: ZikrNotificationController,
    private val prayerAlarmBootstrapper: PrayerAlarmBootstrapper,
    private val adhanPreviewPlayer: AdhanPreviewPlayer
) : ViewModel() {

    private val storedSettings = combine(
        settingsRepository.fontSize,
        settingsRepository.isVibrationEnabled,
        settingsRepository.adhanSound,
        settingsRepository.zikrNotificationEnabled,
        settingsRepository.zikrInterval
    ) { fontSize, vibration, adhan, zikrEnabled, zikrInterval ->
        SettingsUiState(
            fontSize = fontSize,
            isVibrationEnabled = vibration,
            adhanSoundId = adhan,
            isZikrNotificationEnabled = zikrEnabled,
            zikrIntervalMinutes = zikrInterval
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        storedSettings,
        settingsRepository.themeMode,
        widgetSettings.config,
        adhanPreviewPlayer.playingSoundId
    ) { stored, theme, widget, previewing ->
        stored.copy(themeMode = theme, widget = widget, previewingSoundId = previewing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.FontSizeChanged -> launch { settingsRepository.saveFontSize(action.size) }
            is SettingsAction.VibrationToggled -> launch { settingsRepository.setVibrationEnabled(action.enabled) }
            is SettingsAction.ThemeSelected -> launch { settingsRepository.setThemeMode(action.mode) }

            is SettingsAction.AdhanSelected -> selectAdhan(action.soundId)
            is SettingsAction.AdhanPreviewToggled -> adhanPreviewPlayer.toggle(action.soundId)
            SettingsAction.AdhanPreviewStopped -> adhanPreviewPlayer.stop()

            is SettingsAction.ZikrNotificationToggled -> toggleZikrNotification(action.enabled)
            is SettingsAction.ZikrIntervalSelected -> selectZikrInterval(action.minutes)

            is SettingsAction.WidgetStyleSelected -> widgetSettings.setStyle(action.style)
            is SettingsAction.WidgetFontScaleSelected -> widgetSettings.setFontScale(action.percent)
            is SettingsAction.WidgetOpacityChanged -> widgetSettings.setOpacity(action.percent)
        }
    }

    private fun selectAdhan(soundId: String) = launch {
        adhanPreviewPlayer.stop()
        settingsRepository.setAdhanSound(soundId)
        try {
            prayerAlarmBootstrapper.rescheduleAll()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "reschedule after sound change failed", e)
        }
    }

    private fun toggleZikrNotification(enabled: Boolean) = launch {
        settingsRepository.setZikrNotificationEnabled(enabled)
        if (enabled) {
            zikrNotifications.schedule(settingsRepository.zikrInterval.first())
        } else {
            zikrNotifications.cancel()
        }
    }

    private fun selectZikrInterval(minutes: Int) = launch {
        settingsRepository.setZikrInterval(minutes)
        if (settingsRepository.zikrNotificationEnabled.first()) {
            zikrNotifications.schedule(minutes)
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    override fun onCleared() {
        adhanPreviewPlayer.stop()
        super.onCleared()
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}
