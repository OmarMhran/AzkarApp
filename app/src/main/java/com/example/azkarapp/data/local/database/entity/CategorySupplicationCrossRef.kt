package com.example.azkarapp.data.local.database.entity

import androidx.room.Entity


@Entity(tableName = "category_supplication_cross_ref", primaryKeys = ["categoryId", "supplicationId"])
data class CategorySupplicationCrossRef(
    val categoryId: Int,
    val supplicationId: Int
)