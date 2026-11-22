package com.example.azkarapp.data.repo

import kotlinx.coroutines.flow.Flow

interface QiblahRepository {
    /** اتجاه الجهاز (بالدرجات، بالنسبة للشمال المغناطيسي) كل ~33ms. بتقفل لو مفيش حساسات. */
    fun getCompassAzimuthFlow(): Flow<Float>

    /** زاوية القبلة من الشمال الحقيقي لموقع معيّن. */
    fun calculateQiblahBearing(lat: Double, lon: Double): Float
}
