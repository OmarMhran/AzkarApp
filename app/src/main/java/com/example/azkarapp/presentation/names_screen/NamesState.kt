package com.example.azkarapp.presentation.names_screen

import com.example.azkarapp.domain.model.Names

data class NamesState(
    val isLoading: Boolean = false,
    val names: List<Names> = emptyList(),
    val originalNames: List<Names> = emptyList(),
    val searchQuery: String = "",
    val selectedName: Names? = null,
    val error: String = ""
)
