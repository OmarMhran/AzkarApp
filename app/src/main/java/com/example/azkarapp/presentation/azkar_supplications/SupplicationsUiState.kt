package com.example.azkarapp.presentation.azkar_supplications

import com.example.azkarapp.domain.model.AzkarSupplication

data class SupplicationsUiState(
    val categoryName: String = "",
    val isLoading: Boolean = false,
    val supplications: List<AzkarSupplication> = emptyList(),
    /** المتبقي من التكرار لكل ذكر (id → عدد). 0 = خلّص. */
    val currentCounts: Map<Int, Int> = emptyMap(),
    val showCompletionDialog: Boolean = false,
    val error: String? = null,
    /** أول ذكر لسه ما خلصش (علشان نكمل من مكان ما وقف المستخدم). */
    val initialIndex: Int = 0,
    val fontSize: Float = 18f
)
