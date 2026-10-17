package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_azkar")
data class CustomZikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
