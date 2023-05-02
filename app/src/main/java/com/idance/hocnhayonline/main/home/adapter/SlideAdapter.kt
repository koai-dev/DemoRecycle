package com.idance.hocnhayonline.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.databinding.ItemSlideHomeBinding
import com.koaidev.idancesdk.model.SlideItem

class SlideComparator : DiffUtil.ItemCallback<SlideItem>() {
    override fun areItemsTheSame(oldItem: SlideItem, newItem: SlideItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SlideItem, newItem: SlideItem): Boolean =
        oldItem.id == newItem.id
}

class SlideAdapter : ListAdapter<SlideItem, SlideAdapter.SlideVH>(SlideComparator()) {
    interface Callback{
        fun onClickSlideItem(slideItem: SlideItem)
    }
    var callback : Callback? = null
    class SlideVH(val binding: ItemSlideHomeBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideVH {
        return SlideVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_slide_home,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SlideVH, position: Int) {
        holder.binding.slide = getItem(position)
        holder.binding.txtName.isSelected = true
        holder.binding.root.setOnClickListener {
            callback?.onClickSlideItem(getItem(position))
        }
        holder.binding.executePendingBindings()
    }
}