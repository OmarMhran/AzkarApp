package com.example.azkarapp.data.local.assets.model

import kotlinx.serialization.Serializable


@Serializable
data class NamesDto(
    val id: Int,
    val name: String,
    val meaning: String
)