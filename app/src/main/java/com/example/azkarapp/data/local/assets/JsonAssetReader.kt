package com.example.azkarapp.data.local.assets

import android.content.Context
import android.util.Log
import com.example.azkarapp.data.local.assets.model.CategoryDto
import com.example.azkarapp.data.local.assets.model.GroupDto
import com.example.azkarapp.data.local.assets.model.NamesDto
import com.example.azkarapp.data.local.assets.model.SupplicationDto
import com.example.azkarapp.domain.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.InputStreamReader

class JsonAssetReader(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private var cachedGroups: List<GroupDto>? = null
    private var cachedCategories: List<CategoryDto>? = null
    private var cachedSupplications: List<SupplicationDto>? = null
    private var cachedCountries: List<Country>? = null
    private var cachedNames: List<NamesDto>? = null


    suspend fun readNames(): List<NamesDto> = withContext(Dispatchers.IO) {
        if (cachedNames == null) {
            cachedNames = readJsonFromAsset("Allah_Names.json") ?: emptyList()
        }
        cachedNames!!
    }

    suspend fun readGroups(): List<GroupDto> = withContext(Dispatchers.IO) {
        if (cachedGroups == null) {
            cachedGroups = readJsonFromAsset("Section.json") ?: emptyList()
        }
        cachedGroups!!
    }

    suspend fun readCategories(): List<CategoryDto> = withContext(Dispatchers.IO) {
        if (cachedCategories == null) {
            cachedCategories = readJsonFromAsset("Category.json") ?: emptyList()
        }
        cachedCategories!!
    }

    suspend fun readSupplications(): List<SupplicationDto> = withContext(Dispatchers.IO) {
        if (cachedSupplications == null) {
            cachedSupplications = readJsonFromAsset("Supplication.json") ?: emptyList()
        }
        cachedSupplications!!
    }

    suspend fun readCountries(): List<Country> = withContext(Dispatchers.IO) {
        if (cachedCountries == null) {
            cachedCountries = readJsonFromAsset("countries.json") ?: listOf(
                Country(
                    "EG",
                    "مصر",
                    "🇪🇬",
                    5,
                    30.0444,
                    31.2357,
                    "القاهرة",
                    "Africa/Cairo"
                ),
                Country(
                    "SA",
                    "المملكة العربية السعودية",
                    "🇸🇦",
                    4,
                    24.7136,
                    46.6753,
                    "الرياض",
                    "Asia/Riyadh"
                ),
                Country(
                    "OTHER",
                    "دولة أخرى (عالمي)",
                    "🌍",
                    3,
                    30.0444,
                    31.2357,
                    "القاهرة",
                    "Africa/Cairo"
                )
            )
        }
        cachedCountries!!
    }

    private inline fun <reified T> readJsonFromAsset(fileName: String): T? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    json.decodeFromString<T>(reader.readText())
                }
            }
        } catch (e: Exception) {
            Log.e("JsonAssetReader", "failed to read $fileName", e)
            null
        }
    }
}