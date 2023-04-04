/*
 * *
 *  * Created by Nguyễn Kim Khánh on 3/28/23, 5:02 PM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 3/28/23, 5:02 PM
 *
 */

package com.idance.hocnhayonline2023.person.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfilePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm){
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> {
//                MainInformationFragment()
//            }
//            3 -> {
//                DocumentUploadFragment(PODCAST_MODE)
//            }
//            else -> {
//                DocumentUploadFragment()
//            }
//        }
        return Fragment()

    }
}