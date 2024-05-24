package com.example.azkar.models

import java.io.Serializable

data class CategoryItem(
    val favorite: Boolean,
    val id: Int,
    val name: String,
    val nameSearch: String,
    val notification: Notification,
    val supplications: List<Supplication>,
    val weight: Int
):Serializable