package com.example.azkar.ui.home.settings.repo

import com.example.azkar.models.ColorsItem
import com.example.azkar.models.DayNightItem
import com.example.azkar.models.SettingsItem

interface SettingsRepository {


    suspend fun getSettings(): SettingsItem
    fun getAllColors(): ArrayList<ColorsItem>
    fun getAllTheme(): ArrayList<DayNightItem>


    fun getSelectedColors(): Int
    fun getSelectedTheme(): Int
    fun setSelectedColor(color: Int)
    fun setSelectedTheme(theme: Int)
}