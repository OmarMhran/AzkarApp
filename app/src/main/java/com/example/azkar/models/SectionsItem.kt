package com.example.azkar.models

import java.io.Serializable

data class SectionsItem(
    val categories: List<Category>,
    val icon: String,
    val id: Int,
    val name: String,
    val weight: Int
):Serializable