package com.idance.hocnhayonline.main.home

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentHomeBinding
import com.idance.hocnhayonline.main.detail.DetailFragment
import com.idance.hocnhayonline.main.home.adapter.CourseAdapter
import com.idance.hocnhayonline.main.home.adapter.LatestSingleAdapter
import com.idance.hocnhayonline.main.home.adapter.SlideAdapter
import com.idance.hocnhayonline.main.home.viewmodel.HomeViewModel
import com.idance.hocnhayonline.main.search.SearchFragment
import com.idance.hocnhayonline.utils.Constants
import com.koaidev.idancesdk.model.LatestMoviesItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(), LatestSingleAdapter.Callback {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var slideAdapter: SlideAdapter
    private lateinit var lastedUnitAdapter: LatestSingleAdapter
    private lateinit var lastedCourseAdapter: CourseAdapter
    private lateinit var activity: MainActivity

    @Inject
    lateinit var homeViewModel: HomeViewModel
    override fun getBindingView(container: ViewGroup?): ViewBinding {
        return DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
    }

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentHomeBinding
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
        setTopicList()
        setSlide()
        setDiscover()
        setTopicList()
        getData()
        observer()
        setSwipeRefresh()
        setClickListener()
    }

    private fun setClickListener() {
        binding.txtSearch.setOnClickListener {
            activity.addFragment(SearchFragment())
        }
        binding.btnSeeMoreSingle.setOnClickListener {
            activity.tabSingleClick()
        }
        binding.btnSeeMoreCourse.setOnClickListener {
            activity.tabCourseClick()
        }
    }

    private fun setSlide() {
        slideAdapter = SlideAdapter()
        binding.slidePager.adapter = slideAdapter
        TabLayoutMediator(
            binding.slideTabLayout, binding.slidePager
        ) { _, _ -> }.attach()
        var isCounting = true
        binding.slidePager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (isCounting) {
                    isCounting = false
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(3000)
                        if (slideAdapter.itemCount > 0) {
                            if (position < slideAdapter.itemCount - 1) {
                                activity.runOnUiThread {
                                    binding.slidePager.currentItem += 1
                                }
                            } else {
                                activity.runOnUiThread {
                                    binding.slidePager.currentItem = 0
                                }
                            }
                        }
                    }.invokeOnCompletion {
                        isCounting = true
                    }
                }
            }
        })
    }

    private fun setDiscover() {
        binding.menuLevel.title = "Cấp độ"
        binding.menuLevel.thumb =
            "https://img.freepik.com/free-vector/business-rising-up-stairs-success-with-red-arrow-white_1284-42743.jpg?w=740&t=st=1680389397~exp=1680389997~hmac=1bf91f06a698548b197fc3f7d289f07786bb82c911eb89d7778598cab0f98107"
        binding.menuCategory.title = "Thể loại"
        binding.menuCategory.thumb =
            "https://img.freepik.com/free-vector/home-dance-class-abstract-illustration_335657-5306.jpg"
        binding.menuTeacher.title = "Giáo viên"
        binding.menuTeacher.thumb =
            "https://img.freepik.com/free-photo/people-taking-part-dance-therapy-class_23-2149346547.jpg?w=1060&t=st=1680389542~exp=1680390142~hmac=3a48201074fb904010ebc32c214df5d9a7dce8d507307913f7a9e7c59a3d7b0e"
    }

    private fun setTopicList() {
        lastedUnitAdapter = LatestSingleAdapter(homeViewModel, activity)
        lastedUnitAdapter.callback = this
        binding.rcvLastedUnit.adapter = lastedUnitAdapter
        binding.rcvLastedUnit.layoutManager =
            object : LinearLayoutManager(requireContext(), VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }

        lastedCourseAdapter = CourseAdapter()
        binding.rcvLastedCourse.adapter = lastedCourseAdapter
        binding.rcvLastedCourse.layoutManager =
            object : LinearLayoutManager(requireContext(), HORIZONTAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }

    }

    private fun getData() {
        homeViewModel.getHomeContent()
    }

    private fun setSwipeRefresh() {
        binding.swipeLayout.setOnRefreshListener {
            getData()
        }
    }

    private fun observer() {
        homeViewModel.listSlide.observe(activity) {
            if (!it.isNullOrEmpty()) {
                slideAdapter.submitList(it)
            }
            binding.swipeLayout.isRefreshing = false
        }
        homeViewModel.listLastSingleUnit.observe(activity) {
            if (!it.isNullOrEmpty()) {
                lastedUnitAdapter.submitList(it)
            }
            binding.swipeLayout.isRefreshing = false
        }
        homeViewModel.listLastSeries.observe(activity) {
            if (!it.isNullOrEmpty()) {
                lastedCourseAdapter.submitList(it)
            }
            binding.swipeLayout.isRefreshing = false
        }
    }

    override fun onItemSingleClick(latestMoviesItem: LatestMoviesItem) {
        activity.addFragment(DetailFragment().apply { arguments = Bundle().apply {
            putInt(Constants.VIDEO_ID, latestMoviesItem.videosId?.toInt()?:0)
        } })
    }
}