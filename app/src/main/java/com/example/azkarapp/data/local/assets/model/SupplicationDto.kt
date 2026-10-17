package com.example.azkarapp.data.local.assets.model

import kotlinx.serialization.Serializable


@Serializable
data class SupplicationDto(
    val id: Int,
    val repeat: Int,
    val body: String,
    val bodyVocalized: String,
    val note: String
)

@Serializable
data class IdWrapper(
    val id: Int
)

