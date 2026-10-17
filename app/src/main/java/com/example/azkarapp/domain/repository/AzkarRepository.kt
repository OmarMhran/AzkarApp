package com.example.azkarapp.domain.repository

import com.example.azkarapp.domain.model.AzkarCategory
import com.example.azkarapp.domain.model.AzkarGroup
import com.example.azkarapp.domain.model.AzkarSupplication
import com.example.azkarapp.domain.model.Names

interface AzkarRepository {

    suspend fun populateDatabaseIfNeeded()

    suspend fun getGroups(): List<AzkarGroup>

    suspend fun getCategoriesByGroup(groupId: Int): List<AzkarCategory>

    suspend fun getSupplicationsByCategory(categoryId: Int): List<AzkarSupplication>

    suspend fun getNames(): List<Names>
}
