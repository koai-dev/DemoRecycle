package com.idance.hocnhayonline.main.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.idance.hocnhayonline.main.search.result.ResultFragment

class SearchPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int  = 3

    override fun createFragment(position: Int): Fragment {
        return ResultFragment().apply { type = position }
    }
}