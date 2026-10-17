package com.example.azkarapp.presentation.main_hub.prayer_times

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.util.notification.PrayerAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class PrayerTimesViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val alarmScheduler: PrayerAlarmScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private companion object {
        const val REFRESH_INTERVAL_MS = 15 * 60 * 1000L
        const val FRIDAY_NAME = "الجمعة"
        const val DHUHR_NAME = "الظهر"
    }

    private val _uiState = MutableStateFlow(PrayerTimesUiState())
    val uiState: StateFlow<PrayerTimesUiState> = _uiState.asStateFlow()

    private val _countdownTime = MutableStateFlow("00:00:00")
    val countdownTime: StateFlow<String> = _countdownTime.asStateFlow()

    @Volatile private var prayerTimesMap = linkedMapOf<String, String>()
    @Volatile private var lastLocation: PrayerRepository.LocationResult? = null
    @Volatile private var computedDay: LocalDate? = null
    private val notificationStateMap = ConcurrentHashMap<String, Boolean>()

    private val manualRefresh = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val hijriFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy هـ", Locale("ar"))

    init {
        observeNotificationSettings()
        startCountdownTicker()
        startLocationPipeline()
    }

    fun onAction(action: PrayerTimesAction) {
        when (action) {
            PrayerTimesAction.Refresh -> manualRefresh.tryEmit(Unit)
            is PrayerTimesAction.ToggleNotification -> togglePrayerNotification(action.keyName)
        }
    }

    // ---------- الموقع والمواقيت ----------

    @OptIn(FlowPreview::class)
    private fun startLocationPipeline() {
        viewModelScope.launch {
            val quick = prayerRepository.getBestKnownLocation()
            if (quick != null && prayerTimesMap.isEmpty()) applyLocation(quick)
        }

        viewModelScope.launch {
            merge(
                flowOf(Unit),
                prayerRepository.selectedCountryCode.drop(1).map { },
                prayerRepository.networkAvailableFlow().catch { },
                manualRefresh,
                flow { while (true) { delay(REFRESH_INTERVAL_MS); emit(Unit) } }
            )
                .debounce(500)
                .collectLatest { refreshLocation() }
        }
    }

    private suspend fun refreshLocation() {
        val locGranted = prayerRepository.hasLocationPermission()
        val notifGranted = prayerRepository.hasNotificationPermission()
        _uiState.update { it.copy(hasLocationPermission = locGranted, hasNotificationPermission = notifGranted) }

        val location = try {
            prayerRepository.resolveLocation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

        if (location != null) {
            applyLocation(location)
        } else if (prayerTimesMap.isEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPermissionDenied = !prayerRepository.hasLocationPermission(),
                    errorMessage = "تعذر تحديد الموقع. اختر بلدك من الإعدادات."
                )
            }
        }
    }

    private suspend fun applyLocation(location: PrayerRepository.LocationResult) {
        val newTimes = withContext(Dispatchers.Default) { prayerRepository.getPrayerTimes(location) }
        if (newTimes.isEmpty()) return

        val changed = newTimes != prayerTimesMap
        lastLocation = location
        prayerTimesMap = newTimes
        computedDay = LocalDate.now()

        _uiState.update {
            it.copy(
                cityName = location.cityName,
                locationSource = location.source,
                isPermissionDenied = false
            )
        }
        updateStateAndList()
        if (changed) scheduleAllAlarms()
    }

    // ---------- التنبيهات ----------

    private fun observeNotificationSettings() {
        viewModelScope.launch {
            prayerRepository.getPrayerNotificationSettings().collectLatest { savedSettings ->
                savedSettings.forEach { (prayer, isEnabled) ->
                    notificationStateMap[prayer] = isEnabled
                }
                if (prayerTimesMap.isNotEmpty()) {
                    scheduleAllAlarms()
                    updateStateAndList()
                }
            }
        }
    }

    private fun togglePrayerNotification(keyName: String) {
        val realKeyName = if (keyName == FRIDAY_NAME) DHUHR_NAME else keyName
        val newState = !(notificationStateMap[realKeyName] ?: true)

        notificationStateMap[realKeyName] = newState

        val timeStr = prayerTimesMap[realKeyName]
        if (timeStr != null) {
            viewModelScope.launch {
                val selectedSound = settingsRepository.adhanSound.first()
                alarmScheduler.schedulePrayerAlarm(realKeyName, timeStr, newState, selectedSound)
            }
        }

        updateStateAndList()

        viewModelScope.launch(Dispatchers.IO) {
            prayerRepository.savePrayerNotificationState(realKeyName, newState)
        }
    }

    private fun scheduleAllAlarms() {
        val snapshot = prayerTimesMap
        viewModelScope.launch {
            val selectedSound = settingsRepository.adhanSound.first()
            snapshot.forEach { (name, timeStr) ->
                val isEnabled = notificationStateMap[name] ?: true
                alarmScheduler.schedulePrayerAlarm(name, timeStr, isEnabled, selectedSound)
            }
        }
    }

    // ---------- العدّ التنازلي ----------

    private fun startCountdownTicker() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val map = prayerTimesMap
                if (map.isNotEmpty()) {
                    updateCountdownOnly(map)

                    val today = LocalDate.now()
                    if (today != computedDay) {
                        computedDay = today
                        lastLocation?.let { loc -> viewModelScope.launch { applyLocation(loc) } }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun updateCountdownOnly(map: LinkedHashMap<String, String>) {
        val nextPrayerInfo = prayerRepository.calculateNextPrayer(map)
        _countdownTime.value = nextPrayerInfo.countdownFormatted

        if (_uiState.value.nextPrayerName != nextPrayerInfo.name) {
            updateStateAndList()
        }
    }

    private fun updateStateAndList() {
        val map = prayerTimesMap
        if (map.isEmpty()) return

        val isTodayFriday = LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY
        val hijriDateStr = HijrahDate.now().format(hijriFormatter)

        val nextPrayerInfo = prayerRepository.calculateNextPrayer(map)
        val realNextKeyName = if (nextPrayerInfo.name == FRIDAY_NAME) DHUHR_NAME else nextPrayerInfo.name

        val updatedList = map.map { (keyName, time) ->
            val displayName = if (keyName == DHUHR_NAME && isTodayFriday) FRIDAY_NAME else keyName
            PrayerUiModel(
                name = displayName,
                realKeyName = keyName,
                time = time,
                isNext = keyName == realNextKeyName,
                isNotificationEnabled = notificationStateMap[keyName] ?: true
            )
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                prayersList = updatedList,
                nextPrayerName = nextPrayerInfo.name,
                hijriDate = hijriDateStr,
                errorMessage = null
            )
        }
    }
}
