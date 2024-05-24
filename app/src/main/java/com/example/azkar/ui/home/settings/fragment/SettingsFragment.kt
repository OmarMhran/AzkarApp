package com.example.azkar.ui.home.settings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.azkar.R
import com.example.azkar.ui.home.settings.data.SettingColorsAdapter
import com.example.azkar.ui.home.settings.data.SettingThemeAdapter
import com.example.azkar.databinding.FragmentSettingsBinding
import com.example.azkar.ui.home.settings.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    lateinit var _binding: FragmentSettingsBinding
    private val binding get() = _binding

    @Inject
    lateinit var settingColorsAdapter: SettingColorsAdapter

    @Inject
    lateinit var settingThemeAdapter: SettingThemeAdapter

    @Inject
    lateinit var linearLayoutManager: LinearLayoutManager
    private val viewModel by viewModels<SettingsViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setRecyclerView()

        binding.header.headerTitle.text = "الإعدادات"



        binding.settingsPrayerAthan.tvSettingsDropDown.text = "الأذان"


        lifecycleScope.launch {
            viewModel.getColorList()
            viewModel.colorList.observe(viewLifecycleOwner, Observer {
                settingColorsAdapter.differ.submitList(it)

            })
        }

        lifecycleScope.launch {
            viewModel.getThemeList()
            viewModel.themeList.observe(viewLifecycleOwner, Observer {
                settingThemeAdapter.differ.submitList(it)
            })
        }




        settingThemeAdapter.setOnItemClickListener {
            if (it == 0) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                settingThemeAdapter.focusedItem = 0

            } else {
                AppCompatDelegate.setDefaultNightMode(it)
                settingThemeAdapter.focusedItem = AppCompatDelegate.getDefaultNightMode()
            }

            settingThemeAdapter.notifyDataSetChanged()

        }


        settingColorsAdapter.setOnItemClickListener {
            val resID = resources.getIdentifier(
                it,
                "style",
                requireContext().packageName
            )
            activity?.setTheme(resID)
//            requireActivity().recreate()
        }

    }

    fun setRecyclerView() {
        settingThemeAdapter = SettingThemeAdapter()
        binding.settingsRowTheme.rvThemeList.apply {
            adapter = settingThemeAdapter
            linearLayoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager
        }

        settingColorsAdapter = SettingColorsAdapter()
        binding.settingsRowColor.rvColorList.apply {
            adapter = settingColorsAdapter
            linearLayoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}
