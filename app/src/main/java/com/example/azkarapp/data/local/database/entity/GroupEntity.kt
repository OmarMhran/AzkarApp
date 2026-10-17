package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "groupsss")
data class GroupEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val icon: String
)