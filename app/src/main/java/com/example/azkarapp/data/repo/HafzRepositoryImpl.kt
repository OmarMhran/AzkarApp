package com.example.azkarapp.data.repo

import android.content.Context
import com.example.azkarapp.data.local.quran.HafzDao
import com.example.azkarapp.data.local.quran.HafzPageEntity
import com.example.azkarapp.data.local.quran.QuranDao
import com.example.azkarapp.domain.model.HafzProgress
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.Surah
import com.example.azkarapp.domain.model.SurahHafz
import com.example.azkarapp.domain.repository.HafzRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HafzRepositoryImpl @Inject constructor(
    private val hafzDao: HafzDao,
    private val quranDao: QuranDao,
    @ApplicationContext private val context: Context
) : HafzRepository {

    private val prefs by lazy { context.getSharedPreferences("hafz_prefs", Context.MODE_PRIVATE) }

    @Volatile
    private var rangesCache: Map<Int, IntRange> = emptyMap()

    private suspend fun ranges(): Map<Int, IntRange> {
        rangesCache.takeIf { it.isNotEmpty() }?.let { return it }
        // جدول الآيات ممكن يكون لسه فاضي قبل أول استيراد، فمش بنخزّن النتيجة الفاضية.
        return hafzDao.surahPageRanges()
            .associate { it.surahNumber to (it.firstPage..it.lastPage) }
            .also { if (it.isNotEmpty()) rangesCache = it }
    }

    override fun observeProgress(): Flow<HafzProgress> =
        combine(quranDao.observeSurahs(), hafzDao.observePages()) { surahEntities, pages ->
            val memorized = pages.toSet()
            val ranges = ranges()
            val surahs = surahEntities.mapNotNull { entity ->
                val range = ranges[entity.number] ?: return@mapNotNull null
                SurahHafz(
                    surah = Surah(
                        number = entity.number,
                        name = entity.name,
                        ayahCount = entity.ayahCount,
                        bismillah = entity.bismillah,
                        isMedinan = entity.isMedinan,
                        startPage = entity.startPage
                    ),
                    firstPage = range.first,
                    lastPage = range.last,
                    memorizedPages = range.count { it in memorized }
                )
            }
            HafzProgress(memorizedPages = memorized, surahs = surahs)
        }

    override suspend fun setPageMemorized(page: Int, memorized: Boolean) {
        if (page !in 1..MushafPage.TOTAL_PAGES) return
        if (memorized) hafzDao.insert(HafzPageEntity(page, System.currentTimeMillis()))
        else hafzDao.delete(page)
    }

    override suspend fun getLastPage(): Int? = withContext(Dispatchers.IO) {
        prefs.getInt(KEY_LAST_PAGE, -1).takeIf { it in 1..MushafPage.TOTAL_PAGES }
    }

    override suspend fun saveLastPage(page: Int) {
        if (page !in 1..MushafPage.TOTAL_PAGES) return
        withContext(Dispatchers.IO) { prefs.edit().putInt(KEY_LAST_PAGE, page).apply() }
    }

    private companion object {
        const val KEY_LAST_PAGE = "last_page"
    }
}