package com.example.azkarapp.data.local.quran

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** صفحة المصحف المعلَّمة كمحفوظة. وجود الصف = الصفحة محفوظة. */
@Entity(tableName = "hafz_pages")
data class HafzPageEntity(
    @PrimaryKey val page: Int,
    val memorizedAt: Long
)

/** أول وآخر صفحة بتظهر فيها آيات سورة معينة (محسوبة من جدول الآيات نفسه). */
data class SurahPageRangeRow(
    val surahNumber: Int,
    val firstPage: Int,
    val lastPage: Int
)

@Dao
interface HafzDao {

    @Query("SELECT page FROM hafz_pages ORDER BY page")
    fun observePages(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: HafzPageEntity)

    @Query("DELETE FROM hafz_pages WHERE page = :page")
    suspend fun delete(page: Int)

    @Query(
        "SELECT surahNumber AS surahNumber, MIN(page) AS firstPage, MAX(page) AS lastPage " +
            "FROM ayahs GROUP BY surahNumber ORDER BY surahNumber"
    )
    suspend fun surahPageRanges(): List<SurahPageRangeRow>
}
