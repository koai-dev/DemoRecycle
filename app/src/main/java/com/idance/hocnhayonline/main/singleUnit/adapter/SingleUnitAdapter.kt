package com.idance.hocnhayonline.main.singleUnit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.databinding.ItemMovieVerticalBinding
import com.koaidev.idancesdk.model.Movie

class MovieComparator : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem.videosId == newItem.videosId

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
        oldItem.videosId == newItem.videosId

}

class SingleUnitAdapter : ListAdapter<Movie, SingleUnitAdapter.SingleVH>(
    MovieComparator()
) {
    interface Callback {
        fun onClickItem(movie: Movie)
    }

    var callback: Callback? = null

    class SingleVH(val binding: ItemMovieVerticalBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleVH {
        return SingleVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_movie_vertical,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SingleVH, position: Int) {
        holder.binding.movie = getItem(position)
        holder.binding.root.setOnClickListener {
            callback?.onClickItem(getItem(position))
        }
        holder.binding.executePendingBindings()
    }

}