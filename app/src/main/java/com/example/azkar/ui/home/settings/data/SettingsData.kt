package com.example.azkar.ui.home.settings.data

import android.content.Context
import com.example.azkar.R
import com.example.azkar.models.ColorsItem
import com.example.azkar.models.DayNightItem
import com.example.azkar.models.SettingsItem
import com.google.gson.Gson
import java.io.InputStream
import javax.inject.Inject

class SettingsData {


    @Inject lateinit var context: Context

    fun getColorList(): ArrayList<ColorsItem> {
        val list = ArrayList<ColorsItem>()
        list.add(0, ColorsItem(0, R.color.color_accent_1,"AppThemeWithColorScheme1"))
        list.add(1, ColorsItem(1, R.color.color_primary_2,"AppThemeWithColorScheme2"))
        list.add(2, ColorsItem(2, R.color.color_primary_3,"AppThemeWithColorScheme3"))
        list.add(3, ColorsItem(3, R.color.color_primary_4,"AppThemeWithColorScheme4"))
        list.add(4, ColorsItem(4, R.color.color_primary_5,"AppThemeWithColorScheme5"))

        return list
    }

    fun getThemeList(): ArrayList<DayNightItem> {
        val list = ArrayList<DayNightItem>()
        list.add(0, DayNightItem(0, R.drawable.ic_auto))
        list.add(1, DayNightItem(1, R.drawable.ic_sun))
        list.add(2, DayNightItem(2, R.drawable.ic_night_mode))
        return list
    }


    suspend fun getSettings(): SettingsItem {
        val json = readJson("Setting.json")
        return Gson().fromJson(json, SettingsItem::class.java)
    }


    suspend fun readJson(fileName: String): String? {
        val json: String
        try {
            val inputStream: InputStream = context.applicationContext.assets?.open(fileName)!!
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer,Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}