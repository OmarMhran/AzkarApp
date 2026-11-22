package com.example.azkarapp.domain.model

data class Surah(
    val number: Int,
    val name: String,
    val ayahCount: Int,
    val bismillah: String,
    val isMedinan: Boolean,
    val startPage: Int
)

data class Ayah(
    val surahNumber: Int,
    val number: Int,
    val text: String,
    val page: Int,
    val juz: Int
)

data class AyahKey(
    val surahNumber: Int,
    val ayahNumber: Int
)

data class AyahMatch(
    val key: AyahKey,
    val surahName: String,
    val text: String,
    val page: Int
)

data class LastRead(
    val page: Int,
    val surahName: String,
    val ayahNumber: Int
)

sealed interface PageBlock {
    data class SurahTitle(val surah: Surah) : PageBlock
    data class Verses(val ayahs: List<Ayah>) : PageBlock
}

data class MushafPage(
    val number: Int,
    val juz: Int,
    val surahName: String,
    val surahNames: Map<Int, String>,
    val endsWithSurahEnd: Boolean,
    val blocks: List<PageBlock>
) {
    companion object {
        const val TOTAL_PAGES = 604
    }
}

class QuranDataException(message: String) : Exception(message)
