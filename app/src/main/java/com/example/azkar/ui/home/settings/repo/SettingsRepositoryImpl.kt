package com.example.azkar.ui.home.settings.repo

import com.example.azkar.ui.home.settings.data.SettingsData
import com.example.azkar.util.shared.SharedPreferenceDataSource
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val data: SettingsData,
    private val shared: SharedPreferenceDataSource
) : SettingsRepository{

    override suspend fun getSettings() = data.getSettings()

    override fun getAllColors() = data.getColorList()

    override fun getAllTheme() = data.getThemeList()

    override fun getSelectedColors() = shared.getSelectedColors()

    override fun getSelectedTheme() = shared.getSelectedTheme()

    override fun setSelectedColor(color: Int) = shared.setSelectedColor(color)

    override fun setSelectedTheme(theme: Int) = shared.setSelectedTheme(theme)
}