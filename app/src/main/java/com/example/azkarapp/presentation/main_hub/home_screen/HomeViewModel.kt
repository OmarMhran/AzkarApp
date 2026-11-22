package com.example.azkarapp.presentation.main_hub.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.AzkarProgressRepository
import com.example.azkarapp.data.repo.HomeRepository
import com.example.azkarapp.data.repo.MisbahaRepository
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.domain.usecase.quran.ObserveLastReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val misbahaRepository: MisbahaRepository,
    private val prayerRepository: PrayerRepository,
    private val azkarProgressRepository: AzkarProgressRepository,
    private val observeQuranLastRead: ObserveLastReadUseCase
) : ViewModel() {

    private companion object {
        const val COUNTDOWN_REFRESH_MS = 60_000L
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @Volatile private var prayerTimesMap: LinkedHashMap<String, String> = linkedMapOf()
    @Volatile private var lastLocation: PrayerRepository.LocationResult? = null
    @Volatile private var computedDay: LocalDate? = null

    private var countdownJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadDailyContent()
        observeTotalTasbih()
        observeAzkarProgress()
        observeQuranProgress()
        loadPrayerTimes()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> {
                _uiState.update { it.copy(isLoading = true) }
                loadDailyContent()
                loadPrayerTimes()
            }
        }
    }

    // ---------- المحتوى اليومي والعدادات ----------

    private fun loadDailyContent() {
        _uiState.update {
            it.copy(
                dailyHadith = homeRepository.getRandomHadith(),
                dailyZikr = homeRepository.getRandomZikr(),
                dailyAyah = homeRepository.getRandomAyah()
            )
        }
    }

    private fun observeTotalTasbih() {
        viewModelScope.launch {
            misbahaRepository.totalAllTime.collect { total ->
                _uiState.update { it.copy(totalTasbihCount = total) }
            }
        }
    }

    private fun observeAzkarProgress() {
        viewModelScope.launch {
            azkarProgressRepository.progress.collect { progress ->
                _uiState.update { it.copy(azkarProgress = progress) }
            }
        }
    }

    private fun observeQuranProgress() {
        viewModelScope.launch {
            try {
                observeQuranLastRead().collect { last ->
                    _uiState.update { it.copy(quranLastRead = last) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // قاعدة المصحف لسه ما اتجهزتش: الكارت يفضل من غير "أكمل القراءة".
            }
        }
    }

    // ---------- الصلاة القادمة ----------

    private fun loadPrayerTimes() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                if (prayerTimesMap.isEmpty()) _uiState.update { it.copy(isLoading = true) }

                if (prayerTimesMap.isEmpty()) {
                    prayerRepository.getBestKnownLocation()?.let { applyLocation(it) }
                }

                val precise = prayerRepository.resolveLocation()
                if (precise != null) {
                    applyLocation(precise)
                } else if (prayerTimesMap.isEmpty()) {
                    showError()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (prayerTimesMap.isEmpty()) showError()
            }
        }
    }

    private suspend fun applyLocation(location: PrayerRepository.LocationResult) {
        val fetched = withContext(Dispatchers.Default) { prayerRepository.getPrayerTimes(location) }
        if (fetched.isEmpty()) return

        lastLocation = location
        prayerTimesMap = LinkedHashMap(fetched)
        computedDay = LocalDate.now()

        updatePrayerCountdown()
        _uiState.update { it.copy(isLoading = false) }
        startLiveCountdown()
    }

    private fun showError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                nextPrayerName = "عذراً",
                timeRemainingForNextPrayer = "تعذر جلب المواقيت"
            )
        }
    }

    private fun updatePrayerCountdown() {
        val map = prayerTimesMap
        if (map.isEmpty()) return
        val info = prayerRepository.calculateNextPrayer(map)
        _uiState.update {
            it.copy(
                nextPrayerName = info.name,
                timeRemainingForNextPrayer = info.remainingTime
            )
        }
    }

    private fun startLiveCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                delay(COUNTDOWN_REFRESH_MS)

                val loc = lastLocation
                if (LocalDate.now() != computedDay && loc != null) {
                    applyLocation(loc)
                    return@launch
                }
                updatePrayerCountdown()
            }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        loadJob?.cancel()
        super.onCleared()
    }
}
