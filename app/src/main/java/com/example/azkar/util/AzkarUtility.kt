package com.example.azkar.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.InputStream
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject


class AzkarUtility @Inject constructor(
    @ActivityContext context: Context
) {

    private val applicationContext = context


    fun readJSONFromSection(fileName: String): String? {
        val json: String
        try {
            val inputStream: InputStream = applicationContext.applicationContext.assets?.open(fileName)!!

            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }


    fun share(item: String?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, item)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        applicationContext.startActivity(shareIntent)
    }

    fun numbersToArabic(item: Int):String{
        val nf: NumberFormat = NumberFormat.getInstance(Locale("ar"))
       return nf.format(item)
    }
}