package com.example.azkar.ui.home

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.azkar.R
import com.example.azkar.databinding.ActivityHomeBinding
import com.example.azkar.util.LocalizationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    lateinit var _binding: ActivityHomeBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeWithColorScheme1)
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.bottomNavigationView.setupWithNavController(azkarNavHostFragment.findNavController())

        azkarNavHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.splashFragment , R.id.permissionFragment, R.id.locationFragment ->
                        binding.bottomNavigationView.visibility = View.INVISIBLE

                    else ->    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }

    }




    override fun onDestroy() {
        super.onDestroy()

    }


}