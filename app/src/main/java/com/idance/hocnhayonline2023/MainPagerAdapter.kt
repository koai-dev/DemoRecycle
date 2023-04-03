package com.idance.hocnhayonline2023

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.idance.hocnhayonline2023.course.CourseFragment
import com.idance.hocnhayonline2023.home.HomeFragment
import com.idance.hocnhayonline2023.person.PersonFragment
import com.idance.hocnhayonline2023.shortMovie.ShortFragment
import com.idance.hocnhayonline2023.singleUnit.SingleUnitFragment

class MainPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int  = 5

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> SingleUnitFragment()
            2 -> ShortFragment()
            3 -> CourseFragment()
            else -> PersonFragment()
        }
    }
}