package com.example.azkarapp.data.repo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@Singleton
class QiblahRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getCompassAzimuthFlow(): Flow<Float> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer == null || magnetometer == null) {
            close()
            return@callbackFlow
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        var smoothedAzimuth = 0f
        var isFirstReading = true

        var lastEmitNs = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return

                val alpha = 0.15f // Low-pass filter coefficient
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
                }
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                    geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                    geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
                }

                if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    val currentAzimuth = (azimuthInDegrees + 360) % 360

                    if (isFirstReading) {
                        smoothedAzimuth = currentAzimuth
                        isFirstReading = false
                    } else {
                        var delta = currentAzimuth - smoothedAzimuth
                        if (delta > 180) delta -= 360
                        if (delta < -180) delta += 360

                        smoothedAzimuth = (smoothedAzimuth + delta * 0.2f + 360) % 360
                    }

                    if (event.timestamp - lastEmitNs >= 33_000_000L) {
                        lastEmitNs = event.timestamp
                        trySend(smoothedAzimuth)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun calculateQiblahBearing(lat: Double, lon: Double): Float {
        val kaabaLat = Math.toRadians(21.422487)
        val kaabaLon = Math.toRadians(39.826206)
        val userLatRad = Math.toRadians(lat)
        val userLonRad = Math.toRadians(lon)

        val deltaLon = kaabaLon - userLonRad

        val y = sin(deltaLon) * cos(kaabaLat)
        val x = cos(userLatRad) * sin(kaabaLat) - sin(userLatRad) * cos(kaabaLat) * cos(deltaLon)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360
        return bearing
    }
}
