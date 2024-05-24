package com.example.azkar.ui.home.calender.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkar.models.Prayer
import com.example.azkar.ui.home.calender.repo.CalenderRepository
import kotlinx.coroutines.launch

class CalendarViewModel @ViewModelInject constructor(
    private val repository: CalenderRepository
) : ViewModel() {

    private val _prayer: MutableLiveData<List<Prayer>> = MutableLiveData()
    val prayer: LiveData<List<Prayer>>
        get() = _prayer

    private val _nextPrayer: MutableLiveData<Prayer> = MutableLiveData()
    val nextPrayer: LiveData<Prayer>
        get() = _nextPrayer

    private val _date: MutableLiveData<String> = MutableLiveData()
    val date: LiveData<String>
        get() = _date

    private val _goeDate: MutableLiveData<String> = MutableLiveData()
    val goeDate: LiveData<String>
        get() = _goeDate

    private val _country: MutableLiveData<String> = MutableLiveData()
    val country: LiveData<String>
        get() = _country

    fun getHijriData() = viewModelScope.launch {
        _date.postValue(repository.getHijriDate())
    }
    fun getGeoDate() = viewModelScope.launch {
        _goeDate.postValue(repository.getGeoDate())
    }

    fun getPrayerTimes() = viewModelScope.launch {
       _prayer.postValue(repository.getPrayerTimes())
    }
    fun getNextPrayerTime() = viewModelScope.launch {
        _nextPrayer.postValue(repository.getNextPrayerTime())
    }

    fun getCountryName() = viewModelScope.launch {
        _country.postValue(repository.getCountryName())
    }
}