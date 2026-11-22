package com.example.azkarapp.presentation.names_screen

import com.example.azkarapp.domain.model.Names

data class NamesUiState(
    val isLoading: Boolean = false,
    /** الأسماء بعد تطبيق البحث. */
    val names: List<Names> = emptyList(),
    val searchQuery: String = "",
    val selectedName: Names? = null,
    val error: String? = null
)
