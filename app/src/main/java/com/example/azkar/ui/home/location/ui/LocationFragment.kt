package com.example.azkar.ui.home.location.ui

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.azkar.R
import com.example.azkar.ui.location.viewmodel.LocationViewModel
import com.example.azkar.util.Constants.Companion.TAG
import com.example.azkar.worker.AppWorker
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class LocationFragment : Fragment() {
    private lateinit var navController: NavController
    private val locationViewModel: LocationViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        getUserLocation()

        navController.navigate(R.id.action_locationFragment_to_calenderFragment)
    }


    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        val mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateUserLocation(location)
                updateUserCountry(location)
                Log.d(TAG, "not equal null getUserCity: $location")

            } else {
                Log.d(TAG, " getUserCity: equal null")

                val locationRequest = LocationRequest.create()
                locationRequest.apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)

                        locationResult.lastLocation.let {
                            updateUserLocation(it)
                            updateUserCountry(it)
                            Log.d(TAG, " getUserCity: request success")

                        }
                    }
                }
                mFusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        }
    }


    private fun updateUserLocation(location: Location?) {
        location?.run {
            locationViewModel.apply{
                updateUserLatitude(latitude.toString())
                updateUserLongitude(longitude.toString())
                updateUserAltitude(altitude.toString())
            }
        }

    }
    private fun updateUserCountry(location: Location) {
        try {
            val geoCoder = Geocoder(requireContext(), Locale("ar"))
            val address = geoCoder.getFromLocation(location.latitude,location.longitude,1)
            if (!address.isNullOrEmpty()){
                address[0].also{
                    locationViewModel.updateUserCountry(it.adminArea + " - " + it.countryName)
                    Log.d(TAG,"location "+ it.adminArea + " - " + it.countryName)
                }
            }

        }catch (e :Exception){
            Log.d(TAG,"getUserCity: " + e.message)
        }



    }


}