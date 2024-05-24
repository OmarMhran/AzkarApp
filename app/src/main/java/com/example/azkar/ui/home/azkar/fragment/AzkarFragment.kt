package com.example.azkar.ui.home.azkar.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.azkar.R
import com.example.azkar.databinding.FragmentAzkarBinding
import com.example.azkar.ui.home.azkar.data.AzkarAdapter
import com.example.azkar.ui.category.CategoryActivity
import com.example.azkar.ui.home.azkar.viewmodel.AzkarViewModel
import com.example.azkar.util.RecyclerViewItemDecorator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AzkarFragment : Fragment() {
    lateinit var _binding: FragmentAzkarBinding
    private val binding get() = _binding
    @Inject
    lateinit var azkarAdapter: AzkarAdapter

    //    @Inject lateinit var gridLayoutManager: GridLayoutManager
    @Inject
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var straggeredLayoutManager: GridLayoutManager
    private val viewModel by viewModels<AzkarViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_azkar, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()


        azkarAdapter.setOnItemClickListener {
            val intent = Intent(requireActivity(), CategoryActivity::class.java)
            intent.putExtra("azkar", it)
            startActivity(intent)
        }

        lifecycleScope.launch {
            viewModel.getSection()
            viewModel.sectionList.observe(viewLifecycleOwner, Observer {
                azkarAdapter.differ.submitList(it)
            })
        }
    }

    private fun setupRecyclerView() {
        azkarAdapter = AzkarAdapter(requireContext())
        binding.rvAzkar.apply {
            adapter = azkarAdapter


            straggeredLayoutManager = GridLayoutManager(
                requireContext(),2,
                GridLayoutManager.VERTICAL,false
            )

            setHasFixedSize(false)
            layoutManager = straggeredLayoutManager
//            val spaceInPixels = 70
//            this.addItemDecoration(RecyclerViewItemDecorator(spaceInPixels))

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }


}