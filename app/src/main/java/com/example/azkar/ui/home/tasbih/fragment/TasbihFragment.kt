package com.example.azkar.ui.home.tasbih.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.azkar.R
import com.example.azkar.databinding.FragmentTasbihBinding
import com.example.azkar.ui.home.tasbih.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class TasbihFragment : Fragment() {

    lateinit var _binding: FragmentTasbihBinding
    private val binding get() = _binding
    private val viewModel by viewModels<TasbihViewModel>()

    private val vibrator: Vibrator by lazy {
        requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tasbih, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.header.headerTitle.text = getString(R.string.tasbeh)
    }


    override fun onResume() {
        super.onResume()

        binding.tvTasbeh.text = viewModel.getMisbahaCount().toString()
        binding.tvTasbeh.run {
            setOnClickListener {
                text = (text.toString().toInt() + 1).toString()
                if (viewModel.isVibrateOn()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                10,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vibrator.vibrate(10)
                    }
                }
            }
        }

        binding.header.btnRedo.setOnClickListener {
            viewModel.setMisbahaCount(0)
            binding.tvTasbeh.text = "0"

        }
    }


    override fun onPause() {
        super.onPause()
        viewModel.setMisbahaCount(binding.tvTasbeh.text.toString().toInt())
    }


    override fun onDestroyView() {
        super.onDestroyView()

    }
}