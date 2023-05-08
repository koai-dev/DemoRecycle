package com.idance.hocnhayonline.main.detail

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentDetailBinding
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.main.detail.viewmodel.DetailViewModel
import com.idance.hocnhayonline.play.PlayVideoActivity
import com.idance.hocnhayonline.utils.Constants
import com.koaidev.idancesdk.AccountUtil
import com.koaidev.idancesdk.model.SingleDetailsMovie
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : BaseFragment() {
    private lateinit var binding: FragmentDetailBinding

    @Inject
    lateinit var detailViewModel: DetailViewModel
    private lateinit var activity: MainActivity
    private var singleDetailsMovie: SingleDetailsMovie? = null
    override fun getBindingView(container: ViewGroup?): ViewBinding =
        DataBindingUtil.inflate(layoutInflater, R.layout.fragment_detail, container, false)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentDetailBinding
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
        activity = requireActivity() as MainActivity
        getDetail()
        observer()
        setOnClick()
    }

    private fun getDetail() {
        val videoId = arguments?.getInt(Constants.VIDEO_ID)
        detailViewModel.getDetail(videoId.toString(), AccountUtil.getUser().userId)
    }

    private fun observer() {
        detailViewModel.detail.observe(activity) {
            binding.detail = it
            singleDetailsMovie = it
        }
        binding.hasFavorite = AccountUtil.isLogin()
    }

    private fun setOnClick() {
        binding.btnLearnNow.setOnClickListener {
            if (singleDetailsMovie != null && singleDetailsMovie?.videos?.isNotEmpty() == true && singleDetailsMovie?.videos?.get(
                    0
                ) != null
            ) {
                activity.openActivity(
                    PlayVideoActivity::class.java,
                    bundle = Bundle().apply {
                        putString(
                            Constants.VIDEO_URL,
                            singleDetailsMovie?.videos?.get(0)?.fileUrl
                        )
                    })
            }
        }
    }
}