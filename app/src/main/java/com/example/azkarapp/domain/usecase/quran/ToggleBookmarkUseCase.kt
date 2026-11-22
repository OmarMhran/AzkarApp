package com.example.azkarapp.domain.usecase.quran

import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.repository.QuranRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(private val repository: QuranRepository) {
    suspend operator fun invoke(key: AyahKey) = repository.toggleBookmark(key)
}
