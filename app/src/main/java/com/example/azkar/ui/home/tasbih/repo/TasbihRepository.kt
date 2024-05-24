package com.example.azkar.ui.home.tasbih.repo

interface TasbihRepository {

    fun getMisbahaCount(): Int
    fun setMisbahaCount(count: Int)
    fun isVibrateOn(): Boolean
    fun setVibrateOn(boolean: Boolean)
}