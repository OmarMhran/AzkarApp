package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookmarkedKeysUseCase @Inject constructor(private val repository: QuranRepository) {
    operator fun invoke(): Flow<Set<AyahKey>> = repository.observeBookmarkedKeys()
}
