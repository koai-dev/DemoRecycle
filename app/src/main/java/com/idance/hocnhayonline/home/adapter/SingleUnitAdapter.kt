/*
 * *
 *  * Created by Nguyễn Kim Khánh on 3/29/23, 5:51 PM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 3/29/23, 5:51 PM
 *
 */

package com.idance.hocnhayonline.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.databinding.ItemLatestSingleBinding
import com.idance.hocnhayonline.databinding.ItemMoreBinding
import com.idance.hocnhayonline.home.viewmodel.HomeViewModel
import com.koaidev.idancesdk.model.LatestMoviesItem

class LastSingleUnitComparator : DiffUtil.ItemCallback<LatestMoviesItem>() {
    override fun areItemsTheSame(oldItem: LatestMoviesItem, newItem: LatestMoviesItem): Boolean =
        oldItem.videosId == newItem.videosId

    override fun areContentsTheSame(oldItem: LatestMoviesItem, newItem: LatestMoviesItem): Boolean =
        oldItem.videosId == newItem.videosId

}

class LatestSingleAdapter(
    private val homeViewModel: HomeViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<LatestMoviesItem, ViewHolder>(LastSingleUnitComparator()) {
    companion object {
        const val TYPE_SINGLE = 0
        const val TYPE_MORE = 1
        lateinit var callback: Callback
    }

    interface Callback {
        fun onItemSingleClick(latestMoviesItem: LatestMoviesItem)
    }

    class LatestSingleVH(val binding: ItemLatestSingleBinding) : ViewHolder(binding.root)
    class MoreVH(val binding: ItemMoreBinding) : ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size - 1 && currentList.size > 3 || getItem(position).videosId == null) {
            TYPE_MORE
        } else {
            TYPE_SINGLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TYPE_MORE) {
            MoreVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_more,
                    parent,
                    false
                )
            )
        } else {
            LatestSingleVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_latest_single,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_MORE) {
            holder as MoreVH
            holder.binding.btnMore.setOnClickListener {
                homeViewModel.showFullListSingle.value = !homeViewModel.showFullListSingle.value!!
                holder.binding.show = homeViewModel.showFullListSingle.value
            }
            holder.binding.executePendingBindings()
        } else {
            holder as LatestSingleVH
            holder.binding.movie = getItem(position)
            holder.binding.position = position
            holder.binding.txtTitle.isSelected = true
            homeViewModel.showFullListSingle.observe(lifecycleOwner) {
                if (homeViewModel.showFullListSingle.value != true && position >= 2) {

                    var layoutParam = holder.itemView.layoutParams
                    if (layoutParam == null) {
                        layoutParam = LayoutParams(LayoutParams.MATCH_PARENT, 0)
                    }
                    layoutParam.height = 0
                    holder.itemView.layoutParams = layoutParam
                } else if (position < 2) {
                    var layoutParam = holder.itemView.layoutParams
                    if (layoutParam == null) {
                        layoutParam =
                            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    }
                    layoutParam.height = LayoutParams.WRAP_CONTENT
                    holder.itemView.layoutParams = layoutParam
                } else if (it) {
                    var layoutParam = holder.itemView.layoutParams
                    if (layoutParam == null) {
                        layoutParam =
                            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    }
                    layoutParam.height = LayoutParams.WRAP_CONTENT
                    holder.itemView.layoutParams = layoutParam
                }
            }

            holder.binding.root.setOnClickListener {
                callback.onItemSingleClick(getItem(position))
            }

            holder.binding.executePendingBindings()
        }
    }
}
