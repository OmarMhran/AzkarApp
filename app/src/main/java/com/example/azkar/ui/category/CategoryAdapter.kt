package com.example.azkar.ui.category

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.azkar.databinding.SearchListItemBinding
import com.example.azkar.models.CategoryItem
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class CategoryAdapter @Inject constructor(
    @ActivityContext context: Context
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    val applicationContext = context
    var icon: String? = null


    private val differCallback = object : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
                SearchListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((CategoryItem) -> Unit)? = null

    @SuppressLint("SetTextI18n", "ResourceType")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val azkar = differ.currentList[position]
        holder.bind(azkar)
    }

    inner class CategoryViewHolder(private val binding: SearchListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryItem: CategoryItem){
            binding.tvCategoryName.text = categoryItem.name
            val resID = applicationContext.resources.getIdentifier(icon, "drawable", applicationContext.packageName)
            binding.ivCategoryIcon.setImageResource(resID)
            binding.ivCategoryItemBookmark.isSelected = categoryItem.favorite

            binding.ivCategoryItemBookmark.setOnClickListener {
                binding.ivCategoryItemBookmark.isSelected = !categoryItem.favorite
            }

            binding.root.setOnClickListener {
                onItemClickListener?.let { it(categoryItem) }
            }
        }

    }


    fun setOnItemClickListener(listener: (CategoryItem) -> Unit) {
        onItemClickListener = listener
    }

}