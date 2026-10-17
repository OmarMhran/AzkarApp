package com.example.azkarapp.presentation.azkar_supplications

import com.example.azkarapp.domain.model.AzkarSupplication

data class SupplicationState(
    val isLoading: Boolean = false,
    val supplications: List<AzkarSupplication> = emptyList(),
    val currentCounts: Map<Int, Int> = emptyMap(),
    val showCompletionDialog: Boolean = false,
    val error: String? = null,
    val initialIndex: Int = 0
)