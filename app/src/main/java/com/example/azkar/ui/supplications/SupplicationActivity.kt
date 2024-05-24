package com.example.azkar.ui.supplications

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.azkar.R
import com.example.azkar.databinding.ActivitySupplicationBinding
import com.example.azkar.models.CategoryItem
import com.example.azkar.ui.home.azkar.viewmodel.AzkarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_supplication.*
import javax.inject.Inject


@AndroidEntryPoint
class SupplicationActivity : AppCompatActivity() {
    lateinit var _binding: ActivitySupplicationBinding
    private val binding get() = _binding
    private val viewModel by viewModels<AzkarViewModel>()

    @Inject
    lateinit var azkarDetailsAdapter: AzkarDetailsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeWithColorScheme1)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_supplication)


        updateUi()

        val supplications = intent.getSerializableExtra("supplications") as? CategoryItem
        val supplication = supplications

        val toolbar = binding.SupplicationToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.SupplicationToolbar.title = supplication?.name


        azkarDetailsAdapter = AzkarDetailsAdapter()
        binding.vpDetails.adapter = azkarDetailsAdapter

        binding.vpDetails.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                azkarDetailsAdapter.count = 0
                azkarDetailsAdapter.notifyDataSetChanged()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)


            }
        })

        lifecycleScope.launchWhenCreated {
            viewModel.getSupplication(supplications!!)
            viewModel.supplicationList.observe(this@SupplicationActivity, Observer {
                azkarDetailsAdapter.differ.submitList(it)

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun updateUi() {
        val toolbar = binding.SupplicationToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.supplication_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val x = binding.vpDetails.currentItem
        val supp = azkarDetailsAdapter.differ.currentList[x]
        return when (item.itemId) {
            R.id.share -> {

                share(supp.bodyVocalized)
                return true
            }
            R.id.copy -> {
                copy(supp.bodyVocalized)
                Toast.makeText(applicationContext, "click on copy", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun share(item: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, item)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        this.startActivity(shareIntent)
    }


    private fun copy(string: String){
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text",string)
        clipboard.setPrimaryClip(clip)
    }
}
