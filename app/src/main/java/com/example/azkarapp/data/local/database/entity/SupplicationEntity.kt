package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplications")
data class SupplicationEntity (
    @PrimaryKey val id: Int,
    val repeat: Int,
    val body: String,
    val bodyVocalized: String,
    val note: String
)