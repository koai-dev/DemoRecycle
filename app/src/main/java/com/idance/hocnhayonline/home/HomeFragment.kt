package com.idance.hocnhayonline.home

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentHomeBinding
import com.idance.hocnhayonline.home.adapter.MovieAdapter
import com.idance.hocnhayonline.home.adapter.SlideAdapter
import com.idance.hocnhayonline.model.Movie
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var slideAdapter: SlideAdapter
    private lateinit var lastedUnitAdapter: MovieAdapter
    private lateinit var lastedCourseAdapter: MovieAdapter
    private lateinit var activity: MainActivity
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
        genCategoryData()
    }

    private fun setSlide() {
        slideAdapter = SlideAdapter()
        binding.slidePager.adapter = slideAdapter
        TabLayoutMediator(
            binding.slideTabLayout, binding.slidePager
        ) { _, _ -> }.attach()
        genTopicData()
        binding.slidePager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2500)
                    if (position < slideAdapter.itemCount - 1) {
                        activity.runOnUiThread {
                            binding.slidePager.currentItem += 1
                            binding.slide = slideAdapter.currentList[position].thumb
                        }
                    } else {
                        activity.runOnUiThread {
                            binding.slidePager.currentItem = 0
                            binding.slide = slideAdapter.currentList[0].thumb
                        }
                    }
                }
            }
        })
    }

    private fun genCategoryData() {
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
        lastedCourseAdapter = MovieAdapter(true)
        binding.rcvLastedCourse.adapter = lastedCourseAdapter
        binding.rcvLastedCourse.layoutManager =
            object : LinearLayoutManager(requireContext(), HORIZONTAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }

        lastedUnitAdapter = MovieAdapter(true)
        binding.rcvLastedUnit.adapter = lastedUnitAdapter
        binding.rcvLastedUnit.layoutManager =
            object : LinearLayoutManager(requireContext(), HORIZONTAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
    }

    private fun genTopicData() {
        val list = ArrayList<Movie>()
        list.add(Movie("https://flypro.vn/photos/old_data/1471261197.jpg", "Nhảy hiện đại"))
        list.add(
            Movie(
                "https://images.elipsport.vn/anh-seo-tin-tuc/2021/2/4/nhay-hien-dai-co-ban-1.jpg",
                "Nhảy KPOP"
            )
        )
        list.add(
            Movie(
                "https://unica.vn/upload/landingpage/2400124355_cac-dieu-nhay-hien-dai-duoc-gioi-tre-me-met-nhat-hien-nay_thumb.jpg",
                "Nhảy sexy"
            )
        )
        list.add(
            Movie(
                "https://inhat.vn/hcm/wp-content/uploads/2022/05/trung-t%C3%A2m-d%E1%BA%A1y-nh%E1%BA%A3y-kpop-%E1%BB%9F-tphcm-6-min.jpg",
                "Lên nóc nhà"
            )
        )
        list.add(Movie("https://flypro.vn/photos/old_data/1471261197.jpg", "Nhảy hiện đại"))
        list.add(
            Movie(
                "https://images.elipsport.vn/anh-seo-tin-tuc/2021/2/4/nhay-hien-dai-co-ban-1.jpg",
                "Nhảy KPOP"
            )
        )
        list.add(
            Movie(
                "https://unica.vn/upload/landingpage/2400124355_cac-dieu-nhay-hien-dai-duoc-gioi-tre-me-met-nhat-hien-nay_thumb.jpg",
                "Nhảy sexy"
            )
        )
        list.add(
            Movie(
                "https://inhat.vn/hcm/wp-content/uploads/2022/05/trung-t%C3%A2m-d%E1%BA%A1y-nh%E1%BA%A3y-kpop-%E1%BB%9F-tphcm-6-min.jpg",
                "Lên nóc nhà"
            )
        )
        slideAdapter.submitList(list)
        lastedUnitAdapter.submitList(list)
        lastedCourseAdapter.submitList(list)
    }
}