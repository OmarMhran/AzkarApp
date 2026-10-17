package com.example.azkarapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.prayerPrefs by preferencesDataStore(name = "prayer_preferences")
private val Context.settingsPrefs by preferencesDataStore(name = "app_settings")
private val Context.misbahaPrefs by preferencesDataStore(name = "misbaha_settings")
private val Context.azkarProgressPrefs by preferencesDataStore(name = "azkar_progress_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @PrayerDataStore
    fun providePrayerDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.prayerPrefs

    @Provides
    @Singleton
    @SettingsStore
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsPrefs

    @Provides
    @Singleton
    @MisbahaStore
    fun provideMisbahaDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.misbahaPrefs

    @Provides
    @Singleton
    @AzkarProgressStore
    fun provideAzkarProgressDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.azkarProgressPrefs
}
