package com.idance.hocnhayonline.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.idance.hocnhayonline.main.course.CourseFragment
import com.idance.hocnhayonline.main.home.HomeFragment
import com.idance.hocnhayonline.main.person.PersonFragment
import com.idance.hocnhayonline.community.CommunityFragment
import com.idance.hocnhayonline.main.singleUnit.SingleUnitFragment

class MainPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int  = 4

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> SingleUnitFragment()
//            2 -> CommunityFragment()
            2 -> CourseFragment()
            else -> PersonFragment()
        }
    }
}