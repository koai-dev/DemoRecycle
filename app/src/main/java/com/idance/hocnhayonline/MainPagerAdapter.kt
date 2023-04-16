package com.idance.hocnhayonline

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.idance.hocnhayonline.course.CourseFragment
import com.idance.hocnhayonline.home.HomeFragment
import com.idance.hocnhayonline.person.PersonFragment
import com.idance.hocnhayonline.community.CommunityFragment
import com.idance.hocnhayonline.singleUnit.SingleUnitFragment

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