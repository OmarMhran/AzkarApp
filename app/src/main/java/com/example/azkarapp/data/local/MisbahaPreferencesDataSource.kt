package com.example.azkarapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.azkarapp.di.MisbahaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MisbahaPreferencesDataSource @Inject constructor(
    @MisbahaStore private val dataStore: DataStore<Preferences>
) {
    private val selectedZikrKey = stringPreferencesKey("selected_zikr")
    private val currentCountKey = intPreferencesKey("current_count")

    val selectedZikrFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[selectedZikrKey] ?: DEFAULT_ZIKR
    }

    val currentCountFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[currentCountKey] ?: 0
    }

    suspend fun saveCurrentSession(zikr: String, count: Int) {
        dataStore.edit { preferences ->
            preferences[selectedZikrKey] = zikr
            preferences[currentCountKey] = count
        }
    }

    private companion object {
        const val DEFAULT_ZIKR = "سبحان الله"
    }
}
