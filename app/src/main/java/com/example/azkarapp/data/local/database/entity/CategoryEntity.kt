package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity (
    @PrimaryKey val id: Int,
    val groupId: Int,
    val weight: Int,
    val name: String,
    val nameSearch: String,
    val favorite: Int
)