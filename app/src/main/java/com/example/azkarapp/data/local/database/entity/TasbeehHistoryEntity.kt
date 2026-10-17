package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbeeh_history")
data class TasbeehHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zikrName: String,
    val count: Int,
    val dateString: String,
    val monthString: String,
    val yearString: String,
    val timestamp: Long = System.currentTimeMillis()
)
