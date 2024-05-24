package com.example.azkar.ui.home.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.azkar.R
import com.example.azkar.databinding.FragmentPermissionBinding
import com.example.azkar.util.Constants.Companion.LOCATION_REQUEST_PERMISSION_CODE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PermissionFragment : Fragment() {

    private lateinit var navController: NavController
    lateinit var permissionBinding : FragmentPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        permissionBinding = FragmentPermissionBinding.inflate(inflater,container,false)
        return permissionBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        permissionBinding.btnAccept.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_REQUEST_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                navController.navigate(R.id.action_permissionFragment_to_locationFragment)
            }else{
                Toast.makeText(requireContext(),R.string.please_accept_permission,Toast.LENGTH_LONG).show()
            }
        }
    }

}