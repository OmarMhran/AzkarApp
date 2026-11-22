package com.example.azkarapp.data.repo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.azkarapp.data.local.PrayerPreferencesDataSource
import com.example.azkarapp.data.local.PrayerTimesCalculator
import com.example.azkarapp.data.repo.PrayerRepository.LocationResult
import com.example.azkarapp.domain.repository.CountryRepository
import com.example.azkarapp.domain.util.NextPrayerCalculator
import com.example.azkarapp.util.LocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PrayerRepositoryImpl @Inject constructor(
    private val locationService: LocationService,
    private val prayerPreferences: PrayerPreferencesDataSource,
    private val countryRepository: CountryRepository,
    @ApplicationContext private val context: Context
) : PrayerRepository {

    private companion object {
        const val GPS_TIMEOUT_MS = 8_000L
        const val GEOCODER_TIMEOUT_MS = 4_000L
        const val SAME_CITY_RADIUS_KM = 30f
        const val FALLBACK_CITY = "موقعك الحالي"
    }

    override val selectedCountryCode: Flow<String> = prayerPreferences.selectedCountryCodeFlow

    override suspend fun getSelectedCountryCode(): String {
        return prayerPreferences.selectedCountryCodeFlow.first()
    }


    override fun hasLocationPermission(): Boolean {
        fun granted(p: String) =
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        return granted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                granted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /** قبل أندرويد 13 مفيش إذن صريح للإشعارات، فبيتعتبر ممنوح دايماً. */
    override fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    override fun networkAvailableFlow(): Flow<Unit> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            close()
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(Unit)
            }
        }
        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            close()
            return@callbackFlow
        }
        awaitClose { runCatching { cm.unregisterNetworkCallback(callback) } }
    }


    override suspend fun getBestKnownLocation(): LocationResult? = withContext(Dispatchers.IO) {
        if (hasLocationPermission()) cachedLocation() ?: countryLocation()
        else countryLocation() ?: cachedLocation()
    }


    override suspend fun resolveLocation(): LocationResult? = withContext(Dispatchers.IO) {
        if (hasLocationPermission()) {
            gpsLocation() ?: cachedLocation() ?: countryLocation()
        } else {
            countryLocation() ?: cachedLocation()
        }
    }

    private suspend fun gpsLocation(): LocationResult? {
        if (!hasLocationPermission()) return null

        val location: Location? = try {
            withTimeoutOrNull(GPS_TIMEOUT_MS) { locationService.getCurrentLocation() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        if (location == null) return null

        val lat = location.latitude
        val lon = location.longitude

        val saved = prayerPreferences.getSavedLocation()
        val nearSavedCity = saved
            ?.takeIf { it.cityName.isNotEmpty() && distanceKm(lat, lon, it.lat, it.lon) < SAME_CITY_RADIUS_KM }
            ?.cityName

        val geocoded = if (isOnline()) getCityNameFromCoordinates(lat, lon) else null
        val city = geocoded ?: nearSavedCity ?: FALLBACK_CITY

        prayerPreferences.saveLocationToCache(lat, lon, city)
        return LocationResult(lat, lon, city, LocationSource.GPS)
    }

    private suspend fun cachedLocation(): LocationResult? {
        val saved = prayerPreferences.getSavedLocation() ?: return null
        if (saved.lat == 0.0 && saved.lon == 0.0) return null
        return LocationResult(
            lat = saved.lat,
            lon = saved.lon,
            cityName = saved.cityName.ifEmpty { FALLBACK_CITY },
            source = LocationSource.CACHED
        )
    }

    private suspend fun countryLocation(): LocationResult? {
        val code = prayerPreferences.selectedCountryCodeFlow.first()
        val country = countryRepository.getAllCountries().find { it.code == code } ?: return null
        return LocationResult(
            lat = country.lat,
            lon = country.lon,
            cityName = country.cityName,
            source = LocationSource.COUNTRY,
            timeZoneId = country.timeZoneId
        )
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, r)
        return r[0] / 1000f
    }


    private suspend fun getCityNameFromCoordinates(lat: Double, lon: Double): String? =
        withTimeoutOrNull(GEOCODER_TIMEOUT_MS) {
            try {
                if (!Geocoder.isPresent()) return@withTimeoutOrNull null
                val geocoder = Geocoder(context, Locale("ar"))

                val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) cont.resume(addresses.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        })
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                }

                address?.let {
                    listOfNotNull(it.locality, it.adminArea, it.subAdminArea)
                        .firstOrNull { name -> name.isNotBlank() }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun fetchLocationAndCity(): LocationResult =
        resolveLocation() ?: LocationResult(0.0, 0.0, "تعذر تحديد الموقع", LocationSource.COUNTRY)

    override suspend fun getPrayerTimes(
        location: LocationResult,
        dayOffset: Int
    ): LinkedHashMap<String, String> {
        val tzId = if (location.source == LocationSource.COUNTRY) location.timeZoneId else null
        return getPrayerTimes(location.lat, location.lon, tzId, dayOffset)
    }

    override suspend fun getPrayerTimes(
        lat: Double,
        lon: Double,
        timeZoneId: String?,
        dayOffset: Int
    ): LinkedHashMap<String, String> {
        val selectedCode = prayerPreferences.selectedCountryCodeFlow.first()
        val country = countryRepository.getAllCountries().find { it.code == selectedCode }
        val methodId = country?.methodId ?: prayerPreferences.calculationMethodFlow.first()

        return PrayerTimesCalculator.getPrayerTimes(
            lat = lat,
            lng = lon,
            method = methodId,
            timeZoneId = timeZoneId,
            dayOffset = dayOffset
        )
    }

    override fun calculateNextPrayer(prayerTimesMap: LinkedHashMap<String, String>): NextPrayerInfo =
        NextPrayerCalculator.calculate(prayerTimesMap)


    override suspend fun savePrayerNotificationState(prayerName: String, isEnabled: Boolean) {
        prayerPreferences.savePrayerNotificationState(prayerName, isEnabled)
    }

    override fun getPrayerNotificationSettings(): Flow<Map<String, Boolean>> {
        return prayerPreferences.prayerNotificationsFlow
    }

    override suspend fun hasSavedLocation(): Boolean {
        return prayerPreferences.hasSavedLocation()
    }

    override suspend fun getLocationFromSelectedCountry(): LocationResult {
        return countryLocation() ?: LocationResult(30.0444, 31.2357, "القاهرة", LocationSource.COUNTRY)
    }
}
