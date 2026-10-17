package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.MisbahaPreferencesDataSource
import com.example.azkarapp.data.local.database.dao.MisbahaDao
import com.example.azkarapp.data.local.database.dao.PeriodStat
import com.example.azkarapp.data.local.database.entity.CustomZikrEntity
import com.example.azkarapp.data.local.database.entity.TasbeehHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MisbahaRepositoryImpl @Inject constructor(
    private val preferencesDataSource: MisbahaPreferencesDataSource,
    private val misbahaDao: MisbahaDao
) : MisbahaRepository {

    private val historyMutex = Mutex()

    override val selectedZikr: Flow<String> = preferencesDataSource.selectedZikrFlow
    override val currentCount: Flow<Int> = preferencesDataSource.currentCountFlow

    override suspend fun saveCurrentSession(zikr: String, count: Int) {
        preferencesDataSource.saveCurrentSession(zikr, count)
    }

    override val customAzkar: Flow<List<CustomZikrEntity>> = misbahaDao.getAllCustomAzkar()

    override suspend fun addCustomZikr(name: String) {
        misbahaDao.insertCustomZikr(CustomZikrEntity(name = name))
    }

    override suspend fun updateCustomZikr(id: Int, newName: String) {
        misbahaDao.updateCustomZikr(CustomZikrEntity(id = id, name = newName))
    }

    override suspend fun deleteCustomZikr(id: Int) {
        misbahaDao.deleteCustomZikr(id)
    }

    override val dailyStats: Flow<List<PeriodStat>> = misbahaDao.getDailyStats()
    override val totalAllTime: Flow<Int> = misbahaDao.getTotalCountAllTime()

    override suspend fun recordHistoryDelta(zikrName: String, delta: Int) {
        if (delta <= 0) return

        // التاريخ بيتحسب لحظة الكتابة (مفيش قيمة متخزنة)، ISO: yyyy-MM-dd
        val today = LocalDate.now()
        val dateString = today.toString()

        historyMutex.withLock {
            val updated = misbahaDao.incrementHistory(
                zikrName = zikrName,
                dateString = dateString,
                delta = delta,
                now = System.currentTimeMillis()
            )
            if (updated == 0) {
                misbahaDao.insertHistory(
                    TasbeehHistoryEntity(
                        zikrName = zikrName,
                        count = delta,
                        dateString = dateString,
                        monthString = YearMonth.from(today).toString(),
                        yearString = today.year.toString()
                    )
                )
            }
        }
    }

    override suspend fun clearAllHistory() {
        misbahaDao.clearAllHistory()
    }
}
