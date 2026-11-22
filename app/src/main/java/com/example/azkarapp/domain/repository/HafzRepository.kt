package com.example.azkarapp.domain.repository

import com.example.azkarapp.domain.model.HafzProgress
import kotlinx.coroutines.flow.Flow

interface HafzRepository {
    fun observeProgress(): Flow<HafzProgress>

    suspend fun setPageMemorized(page: Int, memorized: Boolean)

    suspend fun getLastPage(): Int?
    suspend fun saveLastPage(page: Int)
}