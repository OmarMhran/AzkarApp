package com.example.azkarapp.presentation.main_hub.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.local.AzkarProgress
import com.example.azkarapp.data.local.AzkarProgressDataSource
import com.example.azkarapp.data.repo.HomeRepository
import com.example.azkarapp.data.repo.MisbahaRepository
import com.example.azkarapp.data.repo.PrayerRepository
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
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val dailyHadith: String = "",
    val dailyZikr: String = "",
    val dailyAyah: String = "",
    val totalTasbihCount: Int = 0,
    val nextPrayerName: String = "جاري التحميل...",
    val timeRemainingForNextPrayer: String = "--",
    val azkarProgress: AzkarProgress? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val misbahaRepository: MisbahaRepository,
    private val prayerRepository: PrayerRepository,
    private val azkarProgressDataSource: AzkarProgressDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @Volatile private var prayerTimesMap: LinkedHashMap<String, String> = linkedMapOf()
    @Volatile private var lastLocation: PrayerRepository.LocationResult? = null
    private var computedDay = -1

    private var countdownJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadHomeData()
        observeTotalTasbih()
        loadPrayerTimesAndStartCountdown()
        observeAzkarProgress()
    }

    private fun loadHomeData() {
        _uiState.update { currentState ->
            currentState.copy(
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

    private fun loadPrayerTimesAndStartCountdown() {
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
        computedDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

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
        if (map.isNotEmpty()) {
            val nextPrayerInfo = prayerRepository.calculateNextPrayer(map)
            _uiState.update {
                it.copy(
                    nextPrayerName = nextPrayerInfo.name,
                    timeRemainingForNextPrayer = nextPrayerInfo.remainingTime
                )
            }
        }
    }

    private fun startLiveCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000L)

                val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val loc = lastLocation
                if (today != computedDay && loc != null) {
                    applyLocation(loc)
                    return@launch
                }
                updatePrayerCountdown()
            }
        }
    }

    fun refreshContent() {
        _uiState.update { it.copy(isLoading = true) }
        loadHomeData()
        loadPrayerTimesAndStartCountdown()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        loadJob?.cancel()
    }

    private fun observeAzkarProgress() {
        viewModelScope.launch {
            azkarProgressDataSource.progressFlow.collect { progress ->
                _uiState.update { it.copy(azkarProgress = progress) }
            }
        }
    }
}