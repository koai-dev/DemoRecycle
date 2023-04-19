///*
// * *
// *  * Created by Nguyễn Kim Khánh on 3/29/23, 5:51 PM
// *  * Copyright (c) 2023 . All rights reserved.
// *  * Last modified 3/29/23, 5:51 PM
// *
// */
//
//package com.idance.hocnhayonline.home.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
//import androidx.databinding.DataBindingUtil
//import androidx.lifecycle.LifecycleOwner
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView.ViewHolder
//import anim.dqh.tvw.R
//import anim.dqh.tvw.databinding.ItemYourLibraryMoreBinding
//import anim.dqh.tvw.databinding.ItemYourLibraryProfileBusinessBinding
//import anim.dqh.tvw.wakaBusiness.model.EnterpriseUser
//import anim.dqh.tvw.wakaBusiness.profile.viewmodel.MainInforViewModel
//
//class LibraryComparator : DiffUtil.ItemCallback<EnterpriseUser>() {
//    override fun areItemsTheSame(oldItem: EnterpriseUser, newItem: EnterpriseUser): Boolean =
//        oldItem.id == newItem.id
//
//    override fun areContentsTheSame(oldItem: EnterpriseUser, newItem: EnterpriseUser): Boolean =
//        oldItem.id == newItem.id
//
//}
//
//class LibraryProfileAdapter(private val mainInforViewModel: MainInforViewModel, private val lifecycleOwner: LifecycleOwner) : ListAdapter<EnterpriseUser, ViewHolder>(LibraryComparator()) {
//    companion object {
//        const val TYPE_LIBRARY = 0
//        const val TYPE_MORE = 1
//        lateinit var actionLibrary: ActionLibrary
//    }
//
//    class LibraryVH(val binding: ItemYourLibraryProfileBusinessBinding) : ViewHolder(binding.root)
//    class LibraryMoreVH(val binding: ItemYourLibraryMoreBinding) : ViewHolder(binding.root)
//
//    override fun getItemViewType(position: Int): Int {
//        return if (position == currentList.size - 1 && currentList.size > 3 || getItem(position).id == null) {
//            TYPE_MORE
//        } else {
//            TYPE_LIBRARY
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return if (viewType == TYPE_MORE) {
//            LibraryMoreVH(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_your_library_more, parent, false))
//        } else {
//            LibraryVH(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_your_library_profile_business, parent, false))
//        }
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        if (holder.itemViewType == TYPE_MORE) {
//            holder as LibraryMoreVH
//            holder.binding.btnMore.setOnClickListener {
//                mainInforViewModel.showFullYourLibrary.value = !mainInforViewModel.showFullYourLibrary.value!!
//                holder.binding.show = mainInforViewModel.showFullYourLibrary.value
//            }
//            holder.binding.executePendingBindings()
//        } else {
//            holder as LibraryVH
//            holder.binding.library = getItem(position)
//            holder.binding.position = position
//            holder.binding.txtTitle.isSelected = true
//            mainInforViewModel.showFullYourLibrary.observe(lifecycleOwner) {
////                holder.binding.show = it
//                if (mainInforViewModel.showFullYourLibrary.value != true && position >= 2) {
//
//                    var layoutParam = holder.itemView.layoutParams
//                    if (layoutParam == null) {
//                        layoutParam = LayoutParams(LayoutParams.MATCH_PARENT, 0)
//                    }
//                    layoutParam.height = 0
//                    holder.itemView.layoutParams = layoutParam
//                } else if (position < 2) {
//                    var layoutParam = holder.itemView.layoutParams
//                    if (layoutParam == null) {
//                        layoutParam = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//                    }
//                    layoutParam.height = LayoutParams.WRAP_CONTENT
//                    holder.itemView.layoutParams = layoutParam
//                } else if (it) {
//                    var layoutParam = holder.itemView.layoutParams
//                    if (layoutParam == null) {
//                        layoutParam = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//                    }
//                    layoutParam.height = LayoutParams.WRAP_CONTENT
//                    holder.itemView.layoutParams = layoutParam
//                }
//            }
//
//            holder.binding.root.setOnClickListener {
//                actionLibrary.onClickItemLibrary(getItem(position))
//            }
//
//            holder.binding.executePendingBindings()
//        }
//    }
//}
//
//interface ActionLibrary {
//    fun onClickItemLibrary(enterpriseUser: EnterpriseUser)
//}