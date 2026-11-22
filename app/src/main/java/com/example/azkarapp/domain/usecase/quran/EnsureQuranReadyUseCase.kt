package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.repository.QuranRepository
import javax.inject.Inject

class EnsureQuranReadyUseCase @Inject constructor(private val repository: QuranRepository) {
    suspend operator fun invoke() = repository.ensureImported()
}
