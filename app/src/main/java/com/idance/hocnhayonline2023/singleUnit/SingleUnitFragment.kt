package com.idance.hocnhayonline2023.singleUnit

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline2023.base.BaseFragment
import com.idance.hocnhayonline2023.databinding.FragmentHomeBinding
import com.idance.hocnhayonline2023.databinding.FragmentSingleUnitBinding
import com.idance.hocnhayonline2023.home.adapter.MovieAdapter
import com.idance.hocnhayonline2023.model.Movie
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleUnitFragment : BaseFragment() {
    private lateinit var binding: FragmentSingleUnitBinding
    private lateinit var movieAdapter: MovieAdapter
    override fun getBindingView(): ViewBinding = FragmentSingleUnitBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentSingleUnitBinding
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

        setMovieAdapter()
        genData()
    }

    private fun setMovieAdapter(){
        movieAdapter = MovieAdapter(false)
        binding.rcvMovieSingle.adapter = movieAdapter
    }

    private fun genData(){
        val list = ArrayList<Movie>()
        list.add(Movie("https://flypro.vn/photos/old_data/1471261197.jpg","Nhảy hiện đại"))
        list.add(Movie("https://images.elipsport.vn/anh-seo-tin-tuc/2021/2/4/nhay-hien-dai-co-ban-1.jpg","Nhảy KPOP"))
        list.add(Movie("https://unica.vn/upload/landingpage/2400124355_cac-dieu-nhay-hien-dai-duoc-gioi-tre-me-met-nhat-hien-nay_thumb.jpg","Nhảy sexy"))
        list.add(Movie("https://inhat.vn/hcm/wp-content/uploads/2022/05/trung-t%C3%A2m-d%E1%BA%A1y-nh%E1%BA%A3y-kpop-%E1%BB%9F-tphcm-6-min.jpg","Lên nóc nhà"))
        list.add(Movie("https://flypro.vn/photos/old_data/1471261197.jpg","Nhảy hiện đại"))
        list.add(Movie("https://images.elipsport.vn/anh-seo-tin-tuc/2021/2/4/nhay-hien-dai-co-ban-1.jpg","Nhảy KPOP"))
        list.add(Movie("https://unica.vn/upload/landingpage/2400124355_cac-dieu-nhay-hien-dai-duoc-gioi-tre-me-met-nhat-hien-nay_thumb.jpg","Nhảy sexy"))
        list.add(Movie("https://inhat.vn/hcm/wp-content/uploads/2022/05/trung-t%C3%A2m-d%E1%BA%A1y-nh%E1%BA%A3y-kpop-%E1%BB%9F-tphcm-6-min.jpg","Lên nóc nhà"))
        movieAdapter.submitList(list)
    }
}