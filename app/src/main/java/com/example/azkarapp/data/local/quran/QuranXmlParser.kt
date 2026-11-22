package com.example.azkarapp.data.local.quran

import com.example.azkarapp.domain.model.QuranDataException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

internal class ParsedAyah(val number: Int, val text: String, val bismillah: String)
internal class ParsedSura(val number: Int, val ayahs: List<ParsedAyah>)

/**
 * بيقرا ملف Tanzil بصيغة XML:
 * <sura index="1"><aya index="1" text="..." bismillah="..."/>...</sura>
 * مش بيعدّل في أي نص، وبيتحقق من سلامة البيانات.
 */
internal object QuranXmlParser {

    const val EXPECTED_SURAHS = 114
    const val EXPECTED_AYAHS = 6236

    fun parse(input: InputStream): List<ParsedSura> {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(input, "UTF-8")

        val suras = mutableListOf<ParsedSura>()
        var currentNumber = 0
        var currentAyahs = mutableListOf<ParsedAyah>()
        var inSura = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "sura" -> {
                        currentNumber = parser.getAttributeValue(null, "index")?.toIntOrNull()
                            ?: throw QuranDataException("سورة بدون رقم في ملف المصحف")
                        currentAyahs = mutableListOf()
                        inSura = true
                    }

                    "aya" -> if (inSura) {
                        val number = parser.getAttributeValue(null, "index")?.toIntOrNull()
                            ?: throw QuranDataException("آية بدون رقم في سورة $currentNumber")
                        val text = parser.getAttributeValue(null, "text")
                            ?: throw QuranDataException("آية بدون نص: $currentNumber:$number")
                        val bismillah = parser.getAttributeValue(null, "bismillah").orEmpty()
                        currentAyahs.add(ParsedAyah(number, text, bismillah))
                    }
                }
            } else if (event == XmlPullParser.END_TAG && parser.name == "sura" && inSura) {
                suras.add(ParsedSura(currentNumber, currentAyahs))
                inSura = false
            }
            event = parser.next()
        }
        validate(suras)
        return suras
    }

    private fun validate(suras: List<ParsedSura>) {
        if (suras.size != EXPECTED_SURAHS) {
            throw QuranDataException("عدد السور ${suras.size} بدل $EXPECTED_SURAHS: الملف غير مكتمل")
        }
        var total = 0
        suras.forEachIndexed { i, sura ->
            if (sura.number != i + 1) {
                throw QuranDataException("ترتيب السور غير صحيح عند السورة ${sura.number}")
            }
            if (sura.ayahs.isEmpty()) {
                throw QuranDataException("سورة ${sura.number} بدون آيات")
            }
            sura.ayahs.forEachIndexed { j, ayah ->
                if (ayah.number != j + 1) {
                    throw QuranDataException("ترقيم الآيات غير متسلسل في سورة ${sura.number}")
                }
                if (ayah.text.isBlank()) {
                    throw QuranDataException("آية فارغة: ${sura.number}:${ayah.number}")
                }
            }
            total += sura.ayahs.size
        }
        if (total != EXPECTED_AYAHS) {
            throw QuranDataException("عدد الآيات $total بدل $EXPECTED_AYAHS: الملف غير مكتمل")
        }
    }
}
