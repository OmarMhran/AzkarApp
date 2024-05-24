package com.example.azkar.ui.supplications

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.azkar.databinding.AzkarItemBinding
import com.example.azkar.models.AzkarItem
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.android.synthetic.main.azkar_item.view.*


@ActivityScoped
class AzkarDetailsAdapter: RecyclerView.Adapter<AzkarDetailsAdapter.AzkarDetailsViewHolder>(){

    var count = 0


    private val differCallback = object : DiffUtil.ItemCallback<AzkarItem>() {
        override fun areItemsTheSame(oldItem: AzkarItem, newItem: AzkarItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AzkarItem, newItem: AzkarItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)


    override fun getItemCount(): Int = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AzkarDetailsViewHolder {
        return AzkarDetailsViewHolder(AzkarItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: AzkarDetailsViewHolder, position: Int) {
        val azkar = differ.currentList[position]
        holder.bind(azkar)
        }

    inner class AzkarDetailsViewHolder(private val binding: AzkarItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(azkarItem : AzkarItem){
            binding.tvZekrDetails.text = azkarItem.bodyVocalized
            binding.tvZekrRefDetails.text = azkarItem.note
            binding.tvZekrQuanDetails.text = setCountText(azkarItem.repeat)
            binding.tvProgressCountDetails.text = count.toString()
            binding.supplicationProgress.progress = (count/azkarItem.repeat)*100
            binding.tvZekrCountDetails.text = "${position+1}/${differ.currentList.size}"

            binding.root.setOnClickListener {
                while (count < azkarItem.repeat){
                    count++
                }

                this@AzkarDetailsAdapter.notifyDataSetChanged()
            }
        }
    }





    private fun setCountText(repeat: Int): String{
        return when (repeat) {
            1 -> {
                "مرة واحدة"
            }
            2 -> {
                " مرتان"
            }
            else -> {
                "$repeat مرات "
            }
        }
    }

//    private fun setPercentage(repeat: Int):Int{
//        return (this.click/repeat)*100
//    }


}