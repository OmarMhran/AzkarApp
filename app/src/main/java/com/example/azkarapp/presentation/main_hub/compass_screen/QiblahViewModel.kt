package com.example.azkarapp.presentation.main_hub.compass_screen

import android.hardware.GeomagneticField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.data.repo.QiblahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val qiblahRepository: QiblahRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    @Volatile private var declination = 0f

    private var lastAzimuth: Float? = null
    private var continuousAzimuth = 0f

    init {
        loadLocationAndCalculateQiblah()
        observeCompass()
    }

    private fun loadLocationAndCalculateQiblah() {
        viewModelScope.launch {
            try {
                val location = prayerRepository.getBestKnownLocation()
                    ?: prayerRepository.resolveLocation()

                if (location == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "تعذر تحديد الموقع. اختر بلدك من الإعدادات.")
                    }
                    return@launch
                }

                declination = GeomagneticField(
                    location.lat.toFloat(),
                    location.lon.toFloat(),
                    0f,
                    System.currentTimeMillis()
                ).declination

                val bearing = qiblahRepository.calculateQiblahBearing(
                    lat = location.lat,
                    lon = location.lon
                )

                _uiState.update {
                    it.copy(
                        qiblahBearing = bearing,
                        cityName = location.cityName,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.localizedMessage)
                }
            }
        }
    }

    private fun observeCompass() {
        viewModelScope.launch {
            qiblahRepository.getCompassAzimuthFlow()
                .catch {
                    _uiState.update { it.copy(hasCompassSensor = false) }
                }
                .collect { magneticAzimuth ->
                    val azimuth = (magneticAzimuth + declination + 360f) % 360f
                    val previous = lastAzimuth
                    continuousAzimuth = if (previous == null) azimuth else {
                        var delta = azimuth - previous
                        if (delta > 180f) delta -= 360f else if (delta < -180f) delta += 360f
                        continuousAzimuth + delta
                    }
                    lastAzimuth = azimuth
                    val bearing = _uiState.value.qiblahBearing

                    val diff = abs(azimuth - bearing)
                    val isAligned = diff < 3.5f || diff > 356.5f

                    _uiState.update {
                        it.copy(azimuth = azimuth, isAlignedWithQiblah = isAligned)
                    }
                }
        }
    }
}