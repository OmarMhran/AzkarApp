package com.example.azkar.models

import java.io.Serializable

data class AzkarItem(
    val body: String,
    val bodyVocalized: String,
    val id: Int,
    val note: String,
    val repeat: Int,
    val track: Track
):Serializable