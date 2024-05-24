package com.example.azkar.ui.home.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.azkar.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val navController: NavController by lazy {
        Navigation.findNavController(requireView())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkForPermission()
    }

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            Handler(Looper.getMainLooper()).postDelayed({
                navController.navigate(R.id.action_splashFragment_to_calenderFragment)
            }, 1500)
        }else{
            Handler(Looper.getMainLooper()).postDelayed({
                navController.navigate(R.id.action_splashFragment_to_permissionFragment)
            }, 1500)
        }
    }
}