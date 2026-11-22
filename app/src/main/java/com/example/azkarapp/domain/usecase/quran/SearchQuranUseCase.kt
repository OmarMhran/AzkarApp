package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.AyahMatch
import com.example.azkarapp.domain.repository.QuranRepository
import com.example.azkarapp.domain.util.ArabicNormalizer
import javax.inject.Inject

class SearchQuranUseCase @Inject constructor(private val repository: QuranRepository) {

    suspend operator fun invoke(rawQuery: String): List<AyahMatch> {
        val query = ArabicNormalizer.normalize(rawQuery)
        if (query.length < MIN_QUERY_LENGTH) return emptyList()
        return repository.search(query)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
