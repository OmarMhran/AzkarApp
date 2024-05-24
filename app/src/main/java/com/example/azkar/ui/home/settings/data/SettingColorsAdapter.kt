package com.example.azkar.ui.home.settings.data

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.azkar.databinding.ColorItemBinding
import com.example.azkar.models.ColorsItem
import com.example.azkar.util.ItemClickListener
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.android.synthetic.main.color_item.view.*


@FragmentScoped
class SettingColorsAdapter : RecyclerView.Adapter<SettingColorsAdapter.ColorsViewHolder>() {

    private var focusedItem = 0

    lateinit var itemClickListener: ItemClickListener

    private val differCallback = object : DiffUtil.ItemCallback<ColorsItem>() {
        override fun areItemsTheSame(oldItem: ColorsItem, newItem: ColorsItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ColorsItem, newItem: ColorsItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorsViewHolder {
        return ColorsViewHolder(
                ColorItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((style : String) -> Unit)? = null

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ColorsViewHolder, position: Int) {
        holder.itemView.isSelected = focusedItem == position
        val color = differ.currentList[position]
        holder.bind(color)

    }
    inner class ColorsViewHolder(private val binding: ColorItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(colorsItem: ColorsItem){
            binding.flColorItem.setBackgroundResource(colorsItem.color)
            if (binding.flColorItem.isSelected) {
                binding.ivCheckColor.visibility = View.VISIBLE
            } else {
                binding.ivCheckColor.visibility = View.INVISIBLE
            }
            binding.root.setOnClickListener {
                binding.flColorItem.isSelected = true
                notifyItemChanged(focusedItem)
                focusedItem = position
                notifyItemChanged(focusedItem)


//                itemClickListener.onItemClick(focusedItem)
                onItemClickListener?.let { it(colorsItem.style)}


            }
        }
    }

    fun setOnItemClickListener(listener: (style: String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            val lm = recyclerView.layoutManager

            // Return false if scrolled to the bounds and allow focus to move off the list
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    return@OnKeyListener tryMoveSelection(lm!!, 1)
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    return@OnKeyListener tryMoveSelection(lm!!, -1)
                }
            }
            false
        })
    }
    private fun tryMoveSelection(lm: RecyclerView.LayoutManager, direction: Int): Boolean {
        val tryFocusItem: Int = focusedItem + direction

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < itemCount) {
            notifyItemChanged(focusedItem)
            focusedItem = tryFocusItem
            notifyItemChanged(focusedItem)
            lm.scrollToPosition(focusedItem)
            return true
        }
        return false
    }

    fun setSelectedColor(color :Int) {
        this.focusedItem = color
    }



}

