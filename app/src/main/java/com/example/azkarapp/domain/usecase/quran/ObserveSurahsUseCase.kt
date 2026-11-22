package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.Surah
import com.example.azkarapp.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSurahsUseCase @Inject constructor(private val repository: QuranRepository) {
    operator fun invoke(): Flow<List<Surah>> = repository.observeSurahs()
}
