package com.example.azkarapp.data.local.quran

import com.example.azkarapp.domain.model.QuranDataException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

/** نقطة بداية (سورة، آية) لصفحة أو جزء. */
internal data class StartPoint(val surah: Int, val ayah: Int) {
    /** رقم قابل للمقارنة: أكبر سورة 286 آية فـ 1000 كفاية. */
    val order: Int get() = surah * 1000 + ayah
}

internal class QuranMetadata(
    val ayahCounts: Map<Int, Int>,
    val medinanSurahs: Set<Int>,
    val pages: List<StartPoint>,
    val juzs: List<StartPoint>
)

/**
 * بيقرا quran-data.xml من Tanzil (مسار الـ Quran Metadata): السور والصفحات والأجزاء.
 * <sura index ayas type/> , <page index sura aya/> , <juz index sura aya/>
 */
internal object QuranMetadataParser {

    const val EXPECTED_PAGES = 604
    const val EXPECTED_JUZS = 30

    fun parse(input: InputStream): QuranMetadata {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(input, "UTF-8")

        val ayahCounts = linkedMapOf<Int, Int>()
        val medinan = mutableSetOf<Int>()
        val pages = mutableMapOf<Int, StartPoint>()
        val juzs = mutableMapOf<Int, StartPoint>()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "sura" -> {
                        val index = parser.int("index")
                        ayahCounts[index] = parser.int("ayas")
                        if (parser.getAttributeValue(null, "type") == "Medinan") medinan.add(index)
                    }

                    "page" -> pages[parser.int("index")] = StartPoint(parser.int("sura"), parser.int("aya"))
                    "juz" -> juzs[parser.int("index")] = StartPoint(parser.int("sura"), parser.int("aya"))
                }
            }
            event = parser.next()
        }

        val pageList = ordered(pages, EXPECTED_PAGES, "الصفحات")
        val juzList = ordered(juzs, EXPECTED_JUZS, "الأجزاء")
        if (ayahCounts.size != QuranXmlParser.EXPECTED_SURAHS) {
            throw QuranDataException("ملف البيانات فيه ${ayahCounts.size} سورة بدل ${QuranXmlParser.EXPECTED_SURAHS}")
        }
        if (pageList.first() != StartPoint(1, 1) || juzList.first() != StartPoint(1, 1)) {
            throw QuranDataException("الصفحة أو الجزء الأول لازم يبدأ من الفاتحة آية 1")
        }
        return QuranMetadata(ayahCounts, medinan, pageList, juzList)
    }

    private fun ordered(map: Map<Int, StartPoint>, expected: Int, label: String): List<StartPoint> {
        if (map.size != expected) {
            throw QuranDataException("عدد $label ${map.size} بدل $expected: ملف البيانات غير مكتمل")
        }
        val list = (1..expected).map { map[it] ?: throw QuranDataException("$label: رقم $it ناقص") }
        for (i in 1 until list.size) {
            if (list[i].order <= list[i - 1].order) {
                throw QuranDataException("ترتيب $label غير صحيح عند الرقم ${i + 1}")
            }
        }
        return list
    }

    private fun XmlPullParser.int(name: String): Int =
        getAttributeValue(null, name)?.toIntOrNull()
            ?: throw QuranDataException("قيمة '$name' ناقصة في ملف البيانات")
}
