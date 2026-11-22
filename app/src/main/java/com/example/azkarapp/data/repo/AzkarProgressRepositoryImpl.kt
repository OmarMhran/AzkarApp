package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.AzkarProgress
import com.example.azkarapp.data.local.AzkarProgressDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzkarProgressRepositoryImpl @Inject constructor(
    private val dataSource: AzkarProgressDataSource
) : AzkarProgressRepository {

    override val progress: Flow<AzkarProgress?> = dataSource.progressFlow

    override suspend fun saveProgress(
        categoryId: Int,
        categoryName: String,
        completed: Int,
        total: Int
    ) = dataSource.saveProgress(categoryId, categoryName, completed, total)

    override suspend fun clearProgress(categoryId: Int) = dataSource.clearProgress(categoryId)
}
