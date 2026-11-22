package com.example.azkarapp.domain.model

data class SurahHafz(
    val surah: Surah,
    val firstPage: Int,
    val lastPage: Int,
    val memorizedPages: Int
) {
    val totalPages: Int get() = lastPage - firstPage + 1
    val fraction: Float get() = if (totalPages <= 0) 0f else memorizedPages.toFloat() / totalPages
    val pages: IntRange get() = firstPage..lastPage
}

data class HafzProgress(
    val memorizedPages: Set<Int> = emptySet(),
    val surahs: List<SurahHafz> = emptyList()
) {
    val memorizedCount: Int get() = memorizedPages.size
    val fraction: Float get() = memorizedCount.toFloat() / MushafPage.TOTAL_PAGES

    val firstUnmemorizedPage: Int?
        get() = (1..MushafPage.TOTAL_PAGES).firstOrNull { it !in memorizedPages }
}
