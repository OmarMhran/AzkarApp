package com.example.azkarapp.presentation.azkar_groups

import com.example.azkarapp.domain.model.AzkarGroup

data class AzkarGroupsState(
    val isLoading: Boolean = false,
    val groups: List<AzkarGroup> = emptyList(),
    val error: String = ""
)
