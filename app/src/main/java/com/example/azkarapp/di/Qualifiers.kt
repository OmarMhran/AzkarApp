package com.example.azkarapp.di

import javax.inject.Qualifier

/** DataStore بتاع المواقيت والموقع والـ onboarding (prayer_preferences). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrayerDataStore

/** DataStore بتاع الإعدادات العامة (app_settings). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsStore

/** DataStore بتاع المسبحة (misbaha_settings). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MisbahaStore

/** DataStore بتاع تقدم قراءة الأذكار (azkar_progress_preferences). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AzkarProgressStore
