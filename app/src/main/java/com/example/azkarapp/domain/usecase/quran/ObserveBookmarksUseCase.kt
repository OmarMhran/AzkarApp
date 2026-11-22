package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.AyahMatch
import com.example.azkarapp.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookmarksUseCase @Inject constructor(private val repository: QuranRepository) {
    operator fun invoke(): Flow<List<AyahMatch>> = repository.observeBookmarks()
}
