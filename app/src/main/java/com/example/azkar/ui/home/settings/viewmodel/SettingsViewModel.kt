package com.example.azkar.ui.home.settings.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkar.models.ColorsItem
import com.example.azkar.models.DayNightItem
import com.example.azkar.models.SettingsItem
import com.example.azkar.ui.home.settings.repo.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel @ViewModelInject constructor(
    private val repo: SettingsRepository
) : ViewModel(){

    private val _colorList: MutableLiveData<ArrayList<ColorsItem>> = MutableLiveData()
    val colorList: LiveData<ArrayList<ColorsItem>>
        get() = _colorList

    private val _themeList: MutableLiveData<ArrayList<DayNightItem>> = MutableLiveData()
    val themeList: LiveData<ArrayList<DayNightItem>>
        get() = _themeList



    private val _settingList: MutableLiveData<SettingsItem> = MutableLiveData()
    val settingList: LiveData<SettingsItem>
        get() = _settingList




    fun getColorList() = viewModelScope.launch{
        val list = repo.getAllColors()
        _colorList.postValue(list)
    }

    fun getThemeList() = viewModelScope.launch{
        val list = repo.getAllTheme()
        _themeList.postValue(list)
    }

    fun getSettings() = viewModelScope.launch {
        val settings = repo.getSettings()
        _settingList.postValue( settings)
    }


}