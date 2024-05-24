package com.example.azkar.ui.home.azkar.data

import com.example.azkar.ui.home.azkar.data.AzkarData
import com.example.azkar.models.CategoryItem
import com.example.azkar.models.SectionsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AzkarRepository @Inject constructor(
    val azkarData: AzkarData
) {

    suspend fun getSections() = withContext(Dispatchers.IO){
        azkarData.getSections()
    }

    suspend fun getSupplications() = withContext(Dispatchers.IO){azkarData.getSupplications()}

    suspend fun filterCategory(sectionsItem: SectionsItem) = withContext(Dispatchers.IO){azkarData.filteredCategory(sectionsItem)}

    suspend fun filterSupplications(categoryItem: CategoryItem) =withContext(Dispatchers.IO){
        azkarData.filteredSupplications(categoryItem)}


    suspend fun getCategory() = withContext(Dispatchers.IO){azkarData.getCategory()}

//    suspend fun searchCategory(search : String,list: ArrayList<CategoryItem>) = azkarData.searchCategory(search, list)
}