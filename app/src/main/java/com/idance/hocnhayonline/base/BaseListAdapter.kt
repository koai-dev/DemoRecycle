package com.idance.hocnhayonline.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
class TComparator<T : Any> : DiffUtil.ItemCallback<T>(){
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

}
abstract class BaseListAdapter<T : Any> :  ListAdapter<T, BaseListAdapter.VH>(TComparator<T>()){
    class VH(val binding: ViewBinding) : ViewHolder(binding.root)
    var listener: BaseAction<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(DataBindingUtil.inflate(LayoutInflater.from(parent.context), getLayoutId(viewType), parent, false))
    }

    abstract fun getLayoutId(viewType: Int) : Int

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.root.setOnClickListener {
            listener?.click(position, getItem(position))
        }
    }
    interface BaseAction<T>{
        fun click(position: Int, data: T)

        fun subClick(position: Int, data: T, view: View)
    }
}