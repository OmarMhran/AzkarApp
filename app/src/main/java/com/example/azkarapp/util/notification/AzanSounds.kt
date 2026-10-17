package com.example.azkarapp.util.notification

import com.example.azkarapp.R


object AdhanSounds {
    fun resId(soundId: String): Int = when (soundId) {
        "madina_adhan" -> R.raw.adhan_madinah
        "egypt_adhan" -> R.raw.adhan_egypt
        else -> R.raw.adhan_makkah
    }
}