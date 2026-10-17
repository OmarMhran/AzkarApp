package com.example.azkarapp.domain.model

data class AzkarSupplication(
    val id: Int,
    val repeat: Int,
    val body: String,
    val bodyVocalized: String,
    val note: String
)