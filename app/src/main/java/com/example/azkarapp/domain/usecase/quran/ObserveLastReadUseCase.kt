package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.LastRead
import com.example.azkarapp.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLastReadUseCase @Inject constructor(private val repository: QuranRepository) {
    operator fun invoke(): Flow<LastRead?> = repository.observeLastRead()
}
