package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "names")
data class NamesEntity (
    @PrimaryKey val id: Int,
    val name: String,
    val meaning: String,

)