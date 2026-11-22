package com.example.azkarapp.presentation.azkar_categories

import com.example.azkarapp.domain.model.AzkarCategory

data class CategoriesUiState(
    val sectionName: String = "",
    val isLoading: Boolean = false,
    val categories: List<AzkarCategory> = emptyList(),
    val error: String? = null
)
