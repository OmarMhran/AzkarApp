package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.database.dao.PeriodStat
import com.example.azkarapp.data.local.database.entity.CustomZikrEntity
import kotlinx.coroutines.flow.Flow

interface MisbahaRepository {
    // الجلسة الحالية
    val selectedZikr: Flow<String>
    val currentCount: Flow<Int>
    suspend fun saveCurrentSession(zikr: String, count: Int)

    // الأذكار المخصصة
    val customAzkar: Flow<List<CustomZikrEntity>>
    suspend fun addCustomZikr(name: String)
    suspend fun updateCustomZikr(id: Int, newName: String)
    suspend fun deleteCustomZikr(id: Int)

    // السجل والإحصائيات
    val dailyStats: Flow<List<PeriodStat>>
    val totalAllTime: Flow<Int>
    suspend fun recordHistoryDelta(zikrName: String, delta: Int)
    suspend fun clearAllHistory()
}
