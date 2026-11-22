package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.PageBlock
import com.example.azkarapp.domain.model.QuranDataException
import com.example.azkarapp.domain.repository.QuranRepository
import javax.inject.Inject

class GetMushafPageUseCase @Inject constructor(private val repository: QuranRepository) {

    suspend operator fun invoke(page: Int): MushafPage {
        val ayahs = repository.getPageAyahs(page)
        if (ayahs.isEmpty()) throw QuranDataException("الصفحة $page غير موجودة")
        val surahs = repository.getSurahs().associateBy { it.number }

        val blocks = mutableListOf<PageBlock>()
        var run = mutableListOf<Ayah>()
        fun flush() {
            if (run.isNotEmpty()) blocks.add(PageBlock.Verses(run))
            run = mutableListOf()
        }
        for (ayah in ayahs) {
            if (ayah.number == 1) {
                flush()
                surahs[ayah.surahNumber]?.let { blocks.add(PageBlock.SurahTitle(it)) }
            }
            run.add(ayah)
        }
        flush()

        return MushafPage(
            number = page,
            juz = ayahs.first().juz,
            surahName = surahs[ayahs.first().surahNumber]?.name.orEmpty(),
            surahNames = ayahs.map { it.surahNumber }.distinct()
                .associateWith { surahs[it]?.name.orEmpty() },
            endsWithSurahEnd = ayahs.last().let { last ->
                surahs[last.surahNumber]?.ayahCount == last.number
            },
            blocks = blocks
        )
    }
}
