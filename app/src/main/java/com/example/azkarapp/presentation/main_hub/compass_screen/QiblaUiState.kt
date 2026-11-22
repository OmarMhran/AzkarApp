package com.example.azkarapp.presentation.main_hub.compass_screen

data class QiblaUiState(
    val isLoading: Boolean = true,
    val azimuth: Float = 0f,
    val qiblahBearing: Float = 0f,
    val isAlignedWithQiblah: Boolean = false,
    val cityName: String = "",
    val hasCompassSensor: Boolean = true,
    val errorMessage: String? = null
)
