package com.example.azkar.ui.home.azkar.data


import android.content.Context

import com.example.azkar.models.*

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


import javax.inject.Inject
import kotlin.collections.ArrayList



class AzkarData @Inject constructor(private val context: Context) {


    suspend fun getSections(): Sections {
        return withContext(Dispatchers.IO){
            val listType = object : TypeToken<Sections>() {}.type
            try {
                val inputStream = context.assets.open("Section.json")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val gson = Gson()
                gson.fromJson(reader, listType) as Sections
            } catch (e: IOException) {
                e.printStackTrace()
            } as Sections
//            val json = readJson("Section.json")
//            val list = Gson().fromJson(json, Sections::class.java)
//            return list
        }
        }


    suspend fun getCategory(): CategoryX {
        return withContext(Dispatchers.IO) {
            val listType = object : TypeToken<CategoryX>() {}.type
            try {
                val inputStream = context.assets.open("Category.json")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val gson = Gson()
                gson.fromJson(reader, listType) as CategoryX
            } catch (e: IOException) {
                e.printStackTrace()
            } as CategoryX

//
//        val json = readJson("Category.json")
//        return Gson().fromJson(json, CategoryX::class.java)

        }
    }

    suspend fun filteredCategory(list: SectionsItem): ArrayList<CategoryItem> {
        var i = 0
        val list2 = ArrayList<CategoryItem>()
        return withContext(Dispatchers.IO){
            while (i < list.categories.size) {
                list2.add(
                    i,
                    getCategory().find { it.id == list.categories[i].id }!!
                )
                i++
            }

            list2 as ArrayList<CategoryItem>
        }

    }


//    suspend fun searchCategory(search : String,list: ArrayList<CategoryItem>): ArrayList<CategoryItem>{
//        var i = 0
//        val list2 = ArrayList<CategoryItem>()
//        while (i < list.size) {
//            list2.add(
//                i,
//               list.find {it.nameSearch == search }!!
//            )
//            i++
//        }
//
//        return list2
//    }

    suspend fun getSupplications(): Azkar {
        val json = readJson("Supplication.json")
        return Gson().fromJson(json, Azkar::class.java)
    }

    suspend fun filteredSupplications(list: CategoryItem): ArrayList<AzkarItem> {
        var i = 0
        val list2 = ArrayList<AzkarItem>()
        while (i < list.supplications.size) {
            list2.add(
                i,
                getSupplications().find { it.id == list.supplications[i].id }!!
            )
            i++
        }

        return list2
    }


    suspend fun readJson(fileName: String): String? = withContext(Dispatchers.IO) {
        val json: String
        try {
            val inputStream: InputStream = context.applicationContext.assets?.open(fileName)!!
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return@withContext null
        }
        return@withContext json
    }

}