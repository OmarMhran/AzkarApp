package com.example.azkarapp.data.local.assets.model

import kotlinx.serialization.Serializable


@Serializable
data class GroupDto(
    val id: Int,
    val name: String,
    val icon: String,
    val categories: List<IdWrapper>
)
