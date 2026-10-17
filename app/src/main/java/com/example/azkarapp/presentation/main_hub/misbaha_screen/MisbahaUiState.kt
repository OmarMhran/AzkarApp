package com.example.azkarapp.presentation.main_hub.misbaha_screen

import com.example.azkarapp.data.local.database.dao.PeriodStat
import com.example.azkarapp.data.local.database.entity.CustomZikrEntity

val DefaultAzkar = listOf("سبحان الله", "الحمد لله", "لا إله إلا الله", "الله أكبر")

data class MisbahaUiState(
    val selectedZikr: String = DefaultAzkar.first(),
    val currentCount: Int = 0,
    val customAzkar: List<CustomZikrEntity> = emptyList(),
    val dailyStats: List<PeriodStat> = emptyList(),
    val totalCountAllTime: Int = 0
) {
    val azkarOptions: List<String>
        get() = DefaultAzkar + customAzkar.map { it.name }
}
