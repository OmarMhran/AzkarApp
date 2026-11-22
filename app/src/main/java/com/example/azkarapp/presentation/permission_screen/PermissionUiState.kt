package com.example.azkarapp.presentation.permission_screen

import com.example.azkarapp.domain.model.Country

data class PermissionUiState(
    val countries: List<Country> = emptyList(),
    /** البلد المقترح حسب المنطقة الزمنية للجهاز؛ null لحد ما القائمة تتحمل. */
    val suggestedCountry: Country? = null
)
