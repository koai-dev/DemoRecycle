package com.idance.hocnhayonline.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.databinding.ItemLatestSeriesBinding
import com.koaidev.idancesdk.model.LatestTvseriesItem

class CourseComparator : DiffUtil.ItemCallback<LatestTvseriesItem>() {
    override fun areItemsTheSame(
        oldItem: LatestTvseriesItem, newItem: LatestTvseriesItem
    ): Boolean = oldItem.videosId == newItem.videosId

    override fun areContentsTheSame(
        oldItem: LatestTvseriesItem, newItem: LatestTvseriesItem
    ): Boolean = oldItem.videosId == newItem.videosId


}

class CourseAdapter : ListAdapter<LatestTvseriesItem, CourseAdapter.CourseVH>(CourseComparator()) {
    class CourseVH(val binding: ItemLatestSeriesBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseVH {
        return CourseVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_latest_series, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseVH, position: Int) {
        if (position==0){
            holder.binding.paddingView.visibility = View.VISIBLE
        }
        holder.binding.movie = getItem(position)
        holder.binding.executePendingBindings()
    }
}