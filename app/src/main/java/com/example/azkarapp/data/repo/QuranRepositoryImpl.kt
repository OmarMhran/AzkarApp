package com.example.azkarapp.data.repo

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.azkarapp.data.local.quran.AyahMatchRow
import com.example.azkarapp.data.local.quran.BookmarkEntity
import com.example.azkarapp.data.local.quran.LastReadEntity
import com.example.azkarapp.data.local.quran.QuranDao
import com.example.azkarapp.data.local.quran.QuranImporter
import com.example.azkarapp.data.local.quran.SurahEntity
import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.model.AyahMatch
import com.example.azkarapp.domain.model.LastRead
import com.example.azkarapp.domain.model.Surah
import com.example.azkarapp.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val dao: QuranDao,
    private val importer: QuranImporter
) : QuranRepository {

    override suspend fun ensureImported() = importer.ensureImported()

    override fun observeSurahs(): Flow<List<Surah>> =
        dao.observeSurahs().map { list -> list.map { it.toDomain() } }

    override suspend fun getSurahs(): List<Surah> = dao.getSurahs().map { it.toDomain() }

    override suspend fun getPageAyahs(page: Int): List<Ayah> =
        dao.getPageAyahs(page).map { Ayah(it.surahNumber, it.ayahNumber, it.text, it.page, it.juz) }

    /**
     * كل كلمة في البحث لازم تكون موجودة في الآية (بأي ترتيب)، وبنطابق على النسختين
     * (textSimple / textAlt). مفيش حد أقصى للنتائج.
     */
    override suspend fun search(normalizedQuery: String): List<AyahMatch> {
        val words = normalizedQuery.split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        val where = words.joinToString(" AND ") { "(a.textSimple LIKE ? OR a.textAlt LIKE ?)" }
        val args = words.flatMap { listOf("%$it%", "%$it%") }.toTypedArray()
        val sql = """
            SELECT a.surahNumber AS surahNumber, a.ayahNumber AS ayahNumber,
                   s.name AS surahName, a.text AS text, a.page AS page
            FROM ayahs a JOIN surahs s ON s.number = a.surahNumber
            WHERE $where
            ORDER BY a.surahNumber, a.ayahNumber
        """.trimIndent()
        return dao.searchRaw(SimpleSQLiteQuery(sql, args)).map { it.toDomain() }
    }

    override fun observeBookmarks(): Flow<List<AyahMatch>> =
        dao.observeBookmarks().map { rows -> rows.map { it.toDomain() } }

    override fun observeBookmarkedKeys(): Flow<Set<AyahKey>> =
        dao.observeBookmarkKeys().map { rows ->
            rows.map { AyahKey(it.surahNumber, it.ayahNumber) }.toSet()
        }

    override suspend fun toggleBookmark(key: AyahKey) {
        if (dao.bookmarkCount(key.surahNumber, key.ayahNumber) > 0) {
            dao.deleteBookmark(key.surahNumber, key.ayahNumber)
        } else {
            dao.insertBookmark(
                BookmarkEntity(key.surahNumber, key.ayahNumber, System.currentTimeMillis())
            )
        }
    }

    override fun observeLastRead(): Flow<LastRead?> =
        dao.observeLastRead().map { it?.let { r -> LastRead(r.page, r.surahName, r.ayahNumber) } }

    override suspend fun saveLastRead(page: Int) {
        val first = dao.firstAyahOfPage(page) ?: return
        dao.saveLastRead(
            LastReadEntity(page = page, surahNumber = first.surahNumber, ayahNumber = first.ayahNumber)
        )
    }

    private fun SurahEntity.toDomain() =
        Surah(number, name, ayahCount, bismillah, isMedinan, startPage)

    private fun AyahMatchRow.toDomain() =
        AyahMatch(AyahKey(surahNumber, ayahNumber), surahName, text, page)
}
