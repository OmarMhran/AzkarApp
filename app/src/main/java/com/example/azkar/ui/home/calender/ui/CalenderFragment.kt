package com.example.azkar.ui.home.calender.ui


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.azkar.R
import com.example.azkar.databinding.FragmentCalenderBinding
import com.example.azkar.ui.home.calender.data.PrayerAdapter
import com.example.azkar.ui.home.calender.viewmodel.CalendarViewModel
import com.example.azkar.util.RecyclerViewItemDecorator
import com.example.azkar.worker.AppWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.ExperimentalTime


@AndroidEntryPoint
class CalenderFragment : Fragment() {
    lateinit var _binding : FragmentCalenderBinding
    private val binding get() = _binding

    private val viewModel :CalendarViewModel by viewModels()

    @Inject lateinit var prayerAdapter: PrayerAdapter
    @Inject lateinit var gridLayoutManager: GridLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calender, container, false)
        return binding.root
    }


    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        initAppWorker()

        lifecycleScope.launch {
            viewModel.getPrayerTimes()
            viewModel.prayer.observe(viewLifecycleOwner, Observer {
                prayerAdapter.differ.submitList(it)
            })
        }

        lifecycleScope.launch {
            viewModel.getNextPrayerTime()
            viewModel.nextPrayer.observe(viewLifecycleOwner, Observer {
                binding.prayerCard.tvNextPrayerName.text = it.name
                binding.prayerCard.tvNextPrayerTime.text = it.time

//                val sdf: DateFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
//                val time = sdf.parse(it.time)!!
//                val hours = time.hours
//                val min = time.minutes

            })
        }


        lifecycleScope.launch {
            viewModel.getHijriData()
            viewModel.date.observe(viewLifecycleOwner, Observer {
                binding.tvHijriDate.text = it.toString()
            })
        }

        lifecycleScope.launch {
            viewModel.getGeoDate()
            viewModel.goeDate.observe(viewLifecycleOwner, Observer {
                binding.tvGeogDate.text = it.toString()
            })
        }

        lifecycleScope.launch {
            viewModel.getCountryName()
            viewModel.country.observe(viewLifecycleOwner, Observer {

                binding.prayerCard.tvLocationAddress.text = it

            })
        }
    }


    private fun initAppWorker() {
        val work = PeriodicWorkRequestBuilder<AppWorker>(1, TimeUnit.DAYS).build()
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueueUniquePeriodicWork("UniqueName", ExistingPeriodicWorkPolicy.KEEP,work)

    }


    private fun setupRecyclerView() {
        prayerAdapter = PrayerAdapter(requireContext())
        binding.rvPrayerTimes.apply {
            adapter = prayerAdapter
            
            gridLayoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            val spaceInPixels = 30
            this.addItemDecoration(RecyclerViewItemDecorator(spaceInPixels))

            layoutManager = gridLayoutManager
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()

    }


}