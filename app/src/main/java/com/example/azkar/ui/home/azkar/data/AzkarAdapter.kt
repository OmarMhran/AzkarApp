package com.example.azkar.ui.home.azkar.data

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.azkar.databinding.CategoryAzkarItemBinding
import com.example.azkar.models.SectionsItem
import com.example.azkar.util.AzkarUtility
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class AzkarAdapter @Inject constructor(
    @ActivityContext context: Context
) : RecyclerView.Adapter<AzkarAdapter.AzkarViewHolder>() {

    @Inject lateinit var azkarUtility: AzkarUtility
    val context1 = context


    private val differCallback = object : DiffUtil.ItemCallback<SectionsItem>() {
        override fun areItemsTheSame(oldItem: SectionsItem, newItem: SectionsItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SectionsItem, newItem: SectionsItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AzkarViewHolder {
        return AzkarViewHolder(CategoryAzkarItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((SectionsItem) -> Unit)? = null

    override fun onBindViewHolder(holder: AzkarViewHolder, position: Int) {
        val azkar = differ.currentList[position]
        holder.bind(azkar)

    }


    inner class AzkarViewHolder(private val binding: CategoryAzkarItemBinding): RecyclerView.ViewHolder(binding.root){

        @SuppressLint("SetTextI18n")
        fun bind(sectionsItem: SectionsItem){


            val resID = context1.resources.getIdentifier(
                sectionsItem.icon,
                "drawable",
                context1.packageName
            )

            Glide.with(context1).load(resID).into(binding.ivZekrHome)






            azkarUtility = AzkarUtility(context1)
            binding.tvZekrNameHome.text = sectionsItem.name
            binding.tvZekrQuanHome.text =  azkarUtility.numbersToArabic(sectionsItem.categories.size) + " " + setCountText(sectionsItem.categories.size)



            binding.root.setOnClickListener {
                onItemClickListener?.let { it(sectionsItem)}
            }
        }


    }



    private fun setCountText(repeat: Int): String{
        return when (repeat) {
            in 3..10 -> {
                "أذكار"
            }
            else -> {
                "ذكر "
            }
        }
    }

    fun setOnItemClickListener(listener: (SectionsItem) -> Unit) {
        onItemClickListener = listener
    }

}