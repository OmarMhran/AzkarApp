package com.example.azkar.ui.home.settings.data

import android.annotation.SuppressLint
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.azkar.databinding.DayNightItemBinding
import com.example.azkar.models.DayNightItem
import com.example.azkar.services.OnItemTouchListener
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.android.synthetic.main.category_azkar_item.view.*
import kotlinx.android.synthetic.main.color_item.view.*
import kotlinx.android.synthetic.main.day_night_item.view.*

@FragmentScoped
class SettingThemeAdapter : RecyclerView.Adapter<SettingThemeAdapter.ThemeViewHolder>() {


     var focusedItem = AppCompatDelegate.getDefaultNightMode()
    lateinit var itemTouchListener : OnItemTouchListener


    private val differCallback = object : DiffUtil.ItemCallback<DayNightItem>() {
        override fun areItemsTheSame(oldItem: DayNightItem, newItem: DayNightItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DayNightItem, newItem: DayNightItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        return ThemeViewHolder(
                DayNightItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((style : Int) -> Unit)? = null

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
      holder.itemView.isSelected = focusedItem == position;
      val image = differ.currentList[position]
      holder.bind(image)
    }

    inner class ThemeViewHolder(private val binding: DayNightItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(dayNightItem: DayNightItem){
            binding.ivDayNightItem.setImageResource(dayNightItem.image)

            binding.root.setOnClickListener {
                notifyItemChanged(focusedItem);
                focusedItem = position
                notifyItemChanged(focusedItem);

                onItemClickListener?.let { it(dayNightItem.id)}

            }
        }

    }

    fun setOnItemClickListener(listener: (style: Int) -> Unit) {
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



}