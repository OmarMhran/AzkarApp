package com.example.azkarapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.azkarapp.di.AzkarProgressStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class AzkarProgress(
    val categoryId: Int,
    val categoryName: String,
    val completed: Int,
    val total: Int
)

@Singleton
class AzkarProgressDataSource @Inject constructor(
    @AzkarProgressStore private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val CATEGORY_ID = intPreferencesKey("progress_category_id")
        val CATEGORY_NAME = stringPreferencesKey("progress_category_name")
        val COMPLETED = intPreferencesKey("progress_completed")
        val TOTAL = intPreferencesKey("progress_total")
    }

    val progressFlow: Flow<AzkarProgress?> = dataStore.data.map { prefs ->
        val id = prefs[Keys.CATEGORY_ID] ?: return@map null
        val total = prefs[Keys.TOTAL] ?: 0
        if (total <= 0) return@map null
        AzkarProgress(
            categoryId = id,
            categoryName = prefs[Keys.CATEGORY_NAME] ?: "",
            completed = prefs[Keys.COMPLETED] ?: 0,
            total = total
        )
    }

    suspend fun saveProgress(categoryId: Int, categoryName: String, completed: Int, total: Int) {
        if (total <= 0 || completed <= 0 || completed >= total) return
        dataStore.edit { prefs ->
            prefs[Keys.CATEGORY_ID] = categoryId
            prefs[Keys.CATEGORY_NAME] = categoryName
            prefs[Keys.COMPLETED] = completed
            prefs[Keys.TOTAL] = total
        }
    }

    suspend fun clearProgress(categoryId: Int) {
        dataStore.edit { prefs ->
            if (prefs[Keys.CATEGORY_ID] == categoryId) {
                prefs.clear()
            }
        }
    }
}