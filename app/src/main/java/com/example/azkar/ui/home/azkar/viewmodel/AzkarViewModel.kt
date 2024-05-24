package com.example.azkar.ui.home.azkar.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.azkar.ui.home.azkar.data.AzkarRepository
import com.example.azkar.models.*
//import com.example.azkar.azkarData.repository.AzkarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class AzkarViewModel @ViewModelInject constructor(
    private val repository: AzkarRepository
)  : ViewModel() {



    private val _sectionList: MutableLiveData<Sections> = MutableLiveData()
    val sectionList: LiveData<Sections>
        get() = _sectionList


    private val _categoryList: MutableLiveData<ArrayList<CategoryItem>> = MutableLiveData()
    val categoryList: LiveData<ArrayList<CategoryItem>>
        get() = _categoryList

    private val _supplicationList: MutableLiveData<ArrayList<AzkarItem>> = MutableLiveData()
    val supplicationList: LiveData<ArrayList<AzkarItem>>
        get() = _supplicationList








    fun getSection() = viewModelScope.launch(){
       val list = repository.getSections()
        _sectionList.postValue(list)
    }

    fun getCategory(sectionsItem: SectionsItem) = viewModelScope.launch() {
        val list = repository.filterCategory(sectionsItem)
        _categoryList.postValue(list)
    }

    fun category() = viewModelScope.launch (){
        val list = repository.getCategory()
        _categoryList.postValue(list)
    }

    fun getSupplication(categoryItem: CategoryItem) = viewModelScope.launch() {
        val list = repository.filterSupplications(categoryItem)
        _supplicationList.postValue(list)
    }




//    fun searchCategory(search: String , list: ArrayList<CategoryItem>) = viewModelScope.launch{
//        val searched = repository.searchCategory(search,list)
//        _categoryList.postValue(searched)
//    }

}



