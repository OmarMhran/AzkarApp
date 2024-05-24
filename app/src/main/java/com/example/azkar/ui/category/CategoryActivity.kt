package com.example.azkar.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.azkar.R
import com.example.azkar.databinding.ActivityCategoryBinding
import com.example.azkar.models.SectionsItem
import com.example.azkar.ui.home.azkar.viewmodel.AzkarViewModel
import com.example.azkar.ui.supplications.SupplicationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.toolbar_content_search.view.*
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {
    @Inject
    lateinit var categoryAdapter: CategoryAdapter
    @Inject
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var _binding: ActivityCategoryBinding
    private val binding get() = _binding
    private val viewModel by viewModels<AzkarViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeWithColorScheme1)
        super.onCreate(savedInstanceState)


        _binding = DataBindingUtil.setContentView(this, R.layout.activity_category)
        updateUi()

        val sectionItem = intent.getSerializableExtra("azkar") as? SectionsItem
        val sections = sectionItem
        val image = sections?.icon


        val toolbar = binding.header.baseToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setupRecyclerView()

        binding.header.baseToolbar.title = sections?.name


        lifecycleScope.launchWhenStarted {
            if(sections?.id == 0){
                viewModel.category()
            }else{
                viewModel.getCategory(sections!!)
            }
            viewModel.categoryList.observe(this@CategoryActivity, Observer { azkarResponse ->
                categoryAdapter.differ.submitList(azkarResponse)
                categoryAdapter.icon = image
            })
        }




        categoryAdapter.setOnItemClickListener {
            val intent = Intent(this, SupplicationActivity::class.java)
            intent.putExtra("supplications", it)
            startActivity(intent)

        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(this)
        binding.rvCategory.apply {
            adapter = categoryAdapter
            linearLayoutManager =  LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            layoutManager = linearLayoutManager
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updateUi(){
        val toolbar = binding.header.baseToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }






}