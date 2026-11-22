package com.example.azkarapp.data.local.quran

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun ayahCount(): Int

    @Query("DELETE FROM ayahs")
    suspend fun clearAyahs()

    @Query("DELETE FROM surahs")
    suspend fun clearSurahs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT * FROM surahs ORDER BY number")
    fun observeSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs ORDER BY number")
    suspend fun getSurahs(): List<SurahEntity>

    @Query("SELECT * FROM ayahs WHERE page = :page ORDER BY surahNumber, ayahNumber")
    suspend fun getPageAyahs(page: Int): List<AyahEntity>

    /** البحث بيتبني ديناميكياً (كلمات متعددة) في الـ repository. */
    @RawQuery
    suspend fun searchRaw(query: SupportSQLiteQuery): List<AyahMatchRow>

    @Query(
        """
        SELECT a.surahNumber AS surahNumber, a.ayahNumber AS ayahNumber,
               s.name AS surahName, a.text AS text, a.page AS page
        FROM bookmarks b
        JOIN ayahs a ON a.surahNumber = b.surahNumber AND a.ayahNumber = b.ayahNumber
        JOIN surahs s ON s.number = a.surahNumber
        ORDER BY b.createdAt DESC
        """
    )
    fun observeBookmarks(): Flow<List<AyahMatchRow>>

    @Query("SELECT surahNumber, ayahNumber FROM bookmarks")
    fun observeBookmarkKeys(): Flow<List<AyahKeyRow>>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    suspend fun bookmarkCount(surah: Int, ayah: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    suspend fun deleteBookmark(surah: Int, ayah: Int)

    @Query(
        """
        SELECT l.page AS page, s.name AS surahName, l.ayahNumber AS ayahNumber
        FROM last_read l JOIN surahs s ON s.number = l.surahNumber
        WHERE l.id = 1
        """
    )
    fun observeLastRead(): Flow<LastReadRow?>

    @Query("SELECT * FROM ayahs WHERE page = :page ORDER BY surahNumber, ayahNumber LIMIT 1")
    suspend fun firstAyahOfPage(page: Int): AyahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLastRead(lastRead: LastReadEntity)
}
