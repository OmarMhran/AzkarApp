package com.example.azkarapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.azkarapp.data.local.database.entity.CustomZikrEntity
import com.example.azkarapp.data.local.database.entity.TasbeehHistoryEntity
import kotlinx.coroutines.flow.Flow

data class PeriodStat(
    val period: String,
    val totalCount: Int
)

@Dao
interface MisbahaDao {


    @Query("SELECT * FROM custom_azkar ORDER BY id DESC")
    fun getAllCustomAzkar(): Flow<List<CustomZikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomZikr(zikr: CustomZikrEntity)

    @Update
    suspend fun updateCustomZikr(customZikr: CustomZikrEntity)

    @Query("DELETE FROM custom_azkar WHERE id = :id")
    suspend fun deleteCustomZikr(id: Int)


    @Query(
        "UPDATE tasbeeh_history SET count = count + :delta, timestamp = :now " +
                "WHERE zikrName = :zikrName AND dateString = :dateString"
    )
    suspend fun incrementHistory(zikrName: String, dateString: String, delta: Int, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: TasbeehHistoryEntity)

    @Query(
        """
        SELECT dateString AS period, SUM(count) AS totalCount
        FROM tasbeeh_history
        WHERE dateString >= date('now', 'localtime', '-100 days')
        GROUP BY dateString
        ORDER BY dateString ASC
        """
    )
    fun getDailyStats(): Flow<List<PeriodStat>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM tasbeeh_history")
    fun getTotalCountAllTime(): Flow<Int>

    @Query("DELETE FROM tasbeeh_history")
    suspend fun clearAllHistory()
}
