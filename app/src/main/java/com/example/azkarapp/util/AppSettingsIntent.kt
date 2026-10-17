package com.example.azkarapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** بيفتح صفحة إعدادات التطبيق في النظام (الأذونات). */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
