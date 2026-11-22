package com.example.azkarapp.presentation.main_hub.home_screen

import com.example.azkarapp.data.local.AzkarProgress
import com.example.azkarapp.domain.model.LastRead

data class HomeUiState(
    val isLoading: Boolean = false,
    val dailyHadith: String = "",
    val dailyZikr: String = "",
    val dailyAyah: String = "",
    val totalTasbihCount: Int = 0,
    val nextPrayerName: String = "جاري التحميل...",
    val timeRemainingForNextPrayer: String = "--",
    val azkarProgress: AzkarProgress? = null,
    /** آخر صفحة قرآن المستخدم وقف عندها (لكارت "أكمل القراءة"). */
    val quranLastRead: LastRead? = null
)
