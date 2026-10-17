package com.example.azkarapp.data.local.assets.model

import kotlinx.serialization.Serializable


@Serializable
data class CategoryDto(
    val id: Int,
    val weight: Int,
    val name: String,
    val nameSearch: String,
    val favorite: Boolean,
    val supplications: List<IdWrapper>
)
