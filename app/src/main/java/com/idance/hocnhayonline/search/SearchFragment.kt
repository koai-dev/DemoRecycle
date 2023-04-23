package com.idance.hocnhayonline.search

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentSearchBinding

class SearchFragment : BaseFragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var activity: MainActivity
    override fun getBindingView(): ViewBinding = FragmentSearchBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentSearchBinding
        activity = requireActivity() as MainActivity
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val paramsTop =
                binding.pointTop.layoutParams as ViewGroup.MarginLayoutParams
            paramsTop.setMargins(
                0,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                0,
                0
            )
            binding.pointTop.layoutParams = paramsTop
            insets.consumeSystemWindowInsets()
        }

        setPagerResult()
    }

    private fun setPagerResult(){
        binding.pagerResult.adapter = SearchPagerAdapter(activity)
        TabLayoutMediator(
            binding.tabSearch, binding.pagerResult
        ) { tab, position ->
            if (position==0){
                tab.text = "Bài đơn"
            }else if (position ==1){
                tab.text = "Khóa học"
            }else{
                tab.text = "Giáo viên"
            }
        }.attach()
    }
}