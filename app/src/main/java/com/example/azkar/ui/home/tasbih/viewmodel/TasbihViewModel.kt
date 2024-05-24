package com.example.azkar.ui.home.tasbih.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.azkar.ui.home.tasbih.repo.TasbihRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class TasbihViewModel @ViewModelInject constructor(
    private val repo: TasbihRepository
) :ViewModel(){

    private val _tasbehCount: MutableLiveData<String> = MutableLiveData()
    val tasbehCount: LiveData<String>
        get() = _tasbehCount

    fun getMisbahaCount(): Int = repo.getMisbahaCount()
    fun setMisbahaCount(count: Int) = repo.setMisbahaCount(count)

    fun setVibrateOn(boolean: Boolean) = repo.setVibrateOn(boolean)
    fun isVibrateOn(): Boolean = repo.isVibrateOn()
}