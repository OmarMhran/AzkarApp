package com.example.azkar.ui.home.tasbih.repo

import com.example.azkar.util.shared.SharedPreferenceDataSource
import javax.inject.Inject

class TasbihRepositoryImpl @Inject constructor(
    private val shared: SharedPreferenceDataSource
):TasbihRepository {
    override fun getMisbahaCount() = shared.getMisbahaCount()
    override fun setMisbahaCount(count: Int) = shared.setMisbahaCount(count)
    override fun isVibrateOn(): Boolean = shared.isVibrationOn()
    override fun setVibrateOn(boolean: Boolean) = shared.setVibrationOn(boolean)
}