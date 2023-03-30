package com.koaidev.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.koaidev.myapplication.databinding.ItemDataHorizontalBinding
import com.koaidev.myapplication.databinding.ItemDataVerticalBinding

class DataComparator : DiffUtil.ItemCallback<Data>() {
    override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean = oldItem == newItem
}

class DataAdapter(private val isHorizontal: Boolean) :
    ListAdapter<Data, ViewHolder>(DataComparator()) {
    class DataVerticalVH(val binding: ItemDataVerticalBinding) : ViewHolder(binding.root)
    class DataHorizontalVH(val binding: ItemDataHorizontalBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (isHorizontal) {
            DataHorizontalVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_data_horizontal,
                    parent,
                    false
                )
            )
        } else {
            DataVerticalVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_data_vertical,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHorizontal) {
            holder as DataHorizontalVH
            holder.binding.txtName.text = getItem(position).name
        } else {
            holder as DataVerticalVH
            holder.binding.txtName.text = getItem(position).name
        }
    }

}