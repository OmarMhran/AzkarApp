package com.example.azkarapp.domain.repository

import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.model.AyahMatch
import com.example.azkarapp.domain.model.LastRead
import com.example.azkarapp.domain.model.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {

    suspend fun ensureImported()

    fun observeSurahs(): Flow<List<Surah>>
    suspend fun getSurahs(): List<Surah>
    suspend fun getPageAyahs(page: Int): List<Ayah>

    suspend fun search(normalizedQuery: String): List<AyahMatch>

    fun observeBookmarks(): Flow<List<AyahMatch>>
    fun observeBookmarkedKeys(): Flow<Set<AyahKey>>
    suspend fun toggleBookmark(key: AyahKey)

    fun observeLastRead(): Flow<LastRead?>
    suspend fun saveLastRead(page: Int)
}
