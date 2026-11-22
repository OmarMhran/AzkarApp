package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.AzkarProgress
import kotlinx.coroutines.flow.Flow

/** تقدّم المستخدم في آخر مجموعة أذكار بدأها ولسه ما خلصهاش. */
interface AzkarProgressRepository {
    val progress: Flow<AzkarProgress?>
    suspend fun saveProgress(categoryId: Int, categoryName: String, completed: Int, total: Int)
    suspend fun clearProgress(categoryId: Int)
}
