package com.idance.hocnhayonline.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.databinding.ItemMovieHorizontalBinding
import com.idance.hocnhayonline.databinding.ItemMovieVerticalBinding
import com.idance.hocnhayonline.model.Movie


class MovieAdapter(private val isHorizontal: Boolean) :
    ListAdapter<Movie, ViewHolder>(MovieComparator()) {
    class DataVerticalVH(val binding: ItemMovieVerticalBinding) : ViewHolder(binding.root)
    class DataHorizontalVH(val binding: ItemMovieHorizontalBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (isHorizontal) {
            DataHorizontalVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_movie_horizontal,
                    parent,
                    false
                )
            )
        } else {
            DataVerticalVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_movie_vertical,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHorizontal) {
            holder as DataHorizontalVH
            holder.binding.movie = getItem(position)
            holder.binding.paddingView.visibility = if (position == 0) View.VISIBLE else View.GONE
            holder.binding.executePendingBindings()
        } else {
            holder as DataVerticalVH
            holder.binding.movie = getItem(position)
            holder.binding.executePendingBindings()
        }
    }

}