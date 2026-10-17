package com.example.azkarapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val code: String,
    val name: String,
    val flag: String,
    val methodId: Int,
    val lat: Double,
    val lon: Double,
    val cityName: String,
    val timeZoneId: String
)