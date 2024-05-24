package com.example.azkar.ui.home.calender.data

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.azkar.R
import com.example.azkar.databinding.PrayerItemBinding
import com.example.azkar.models.Prayer
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class PrayerAdapter @Inject constructor(
    @ActivityContext context: Context
) : RecyclerView.Adapter<PrayerAdapter.PrayerViewHolder>() {

    val context1 = context


    private val differCallback = object : DiffUtil.ItemCallback<Prayer>() {
        override fun areItemsTheSame(oldItem: Prayer, newItem: Prayer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Prayer, newItem: Prayer): Boolean {
            return oldItem == newItem
        }
    }



    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        return PrayerViewHolder(
                PrayerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }




    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        val prayer = differ.currentList[position]
        holder.bind(prayer)
    }


    inner class PrayerViewHolder(private val binding: PrayerItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(prayer: Prayer){
            binding.tvPrayerNameItem.text = prayer.name
            binding.tvPrayerTimeItem.text = prayer.time
            val resID = binding.root.resources.getIdentifier(
                prayer.icon,
                "drawable",
                context1.packageName
            )
            binding.ivPrayerIconItem.setImageResource(resID)

            val colorList = listOf<Int>(R.color.color_1,R.color.color_2,R.color.color_3,R.color.color_4,R.color.color_5,R.color.color_6)
            val color  = colorList.get(position)
            binding.cvPrayer.setBackgroundColor(binding.root.resources.getColor(color))

            binding.prayerItemBookmark.isSelected = prayer.notify

            binding.prayerItemBookmark.setOnClickListener {
                if(prayer.notify == true){
                    binding.ivPrayerIconItem.isSelected = false
                }else if(prayer.notify == false){
                    binding.ivPrayerIconItem.isSelected = true
                }

            }

        }
    }

}