package com.example.azkarapp.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareText(context: Context, text: String) {
        val signature = "\n\n--- تم المشاركة عبر تطبيق (أذكار المسلم) ---"
        val fullText = text + signature

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, fullText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة عبر:")
        context.startActivity(shareIntent)
    }
}