package com.idance.hocnhayonline.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.databinding.ItemSlideHomeBinding
import com.idance.hocnhayonline.model.Movie

class MovieComparator : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem.id == newItem.id
}

class SlideAdapter : ListAdapter<Movie, SlideAdapter.SlideVH>(MovieComparator()) {
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
        holder.binding.movie = getItem(position)
        holder.binding.txtName.isSelected = true
        holder.binding.executePendingBindings()
    }
}