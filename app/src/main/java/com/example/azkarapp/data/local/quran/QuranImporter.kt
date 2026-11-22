package com.example.azkarapp.data.local.quran

import android.content.Context
import androidx.room.withTransaction
import com.example.azkarapp.di.IoDispatcher
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.QuranDataException
import com.example.azkarapp.domain.util.ArabicNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: QuranDatabase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val mutex = Mutex()

    /** بيستورد مرة واحدة. لو القاعدة مكتملة بالفعل مبيعملش حاجة. */
    suspend fun ensureImported() = mutex.withLock {
        withContext(ioDispatcher) {
            val dao = database.quranDao()
            if (dao.ayahCount() == QuranXmlParser.EXPECTED_AYAHS) return@withContext

            val suras = readAsset(TEXT_ASSET) { QuranXmlParser.parse(it) }
            val metadata = readAsset(METADATA_ASSET) { QuranMetadataParser.parse(it) }

            // فحص تقاطعي: عدد آيات كل سورة في ملف النص لازم يطابق ملف البيانات.
            suras.forEach { s ->
                if (metadata.ayahCounts[s.number] != s.ayahs.size) {
                    throw QuranDataException(
                        "عدد آيات سورة ${s.number} في النص (${s.ayahs.size}) لا يطابق ملف البيانات"
                    )
                }
            }

            val surahEntities = mutableListOf<SurahEntity>()
            val ayahEntities = ArrayList<AyahEntity>(QuranXmlParser.EXPECTED_AYAHS)

            // بنمشي على الآيات بالترتيب ونحرّك مؤشر الصفحة والجزء كل ما نعدّي حدّ جديد.
            var pageIndex = 0
            var juzIndex = 0
            for (s in suras) {
                var startPage = 0
                for (a in s.ayahs) {
                    val order = StartPoint(s.number, a.number).order
                    while (pageIndex + 1 < metadata.pages.size && metadata.pages[pageIndex + 1].order <= order) pageIndex++
                    while (juzIndex + 1 < metadata.juzs.size && metadata.juzs[juzIndex + 1].order <= order) juzIndex++
                    if (a.number == 1) startPage = pageIndex + 1
                    ayahEntities.add(
                        AyahEntity(
                            surahNumber = s.number,
                            ayahNumber = a.number,
                            text = a.text,
                            textSimple = ArabicNormalizer.normalize(a.text),
                            textAlt = ArabicNormalizer.normalize(a.text, daggerAsAlef = true),
                            page = pageIndex + 1,
                            juz = juzIndex + 1
                        )
                    )
                }
                surahEntities.add(
                    SurahEntity(
                        number = s.number,
                        name = SurahNames.all[s.number - 1],
                        ayahCount = s.ayahs.size,
                        bismillah = s.ayahs.first().bismillah,
                        isMedinan = s.number in metadata.medinanSurahs,
                        startPage = startPage
                    )
                )
            }
            if (pageIndex + 1 != MushafPage.TOTAL_PAGES) {
                throw QuranDataException("آخر صفحة ${pageIndex + 1} بدل ${MushafPage.TOTAL_PAGES}")
            }

            // كله في transaction واحدة: يا إما يكتمل يا إما ما يتحفظش حاجة.
            database.withTransaction {
                dao.clearAyahs()
                dao.clearSurahs()
                dao.insertSurahs(surahEntities)
                dao.insertAyahs(ayahEntities)
            }
        }
    }

    private fun <T> readAsset(path: String, parse: (java.io.InputStream) -> T): T =
        try {
            context.assets.open(path).use(parse)
        } catch (e: FileNotFoundException) {
            throw QuranDataException("ملف غير موجود: assets/$path")
        }

    companion object {
        const val TEXT_ASSET = "quran/quran-uthmani.xml"
        const val METADATA_ASSET = "quran/quran-data.xml"
    }
}
