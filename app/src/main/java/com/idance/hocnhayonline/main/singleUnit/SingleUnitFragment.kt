package com.idance.hocnhayonline.main.singleUnit

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.customView.widgets.EndlessRecyclerViewScrollListener
import com.idance.hocnhayonline.databinding.FragmentSingleUnitBinding
import com.idance.hocnhayonline.databinding.MenuSortBinding
import com.idance.hocnhayonline.main.detail.DetailFragment
import com.idance.hocnhayonline.main.singleUnit.adapter.SingleUnitAdapter
import com.idance.hocnhayonline.main.singleUnit.viewmodel.SingleViewModel
import com.idance.hocnhayonline.utils.Constants
import com.koaidev.idancesdk.model.Movie
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SingleUnitFragment : BaseFragment() {
    private lateinit var binding: FragmentSingleUnitBinding

    @Inject
    lateinit var adapter: SingleUnitAdapter

    @Inject
    lateinit var singleViewModel: SingleViewModel
    private lateinit var activity: MainActivity
    private var nextPage = 1
    private var loadMore: Boolean = true
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener


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

        activity = requireActivity() as MainActivity

        setMovieAdapter()
        getData(nextPage)
        setClickListener()
        observer()
        setUpLoadMore()
    }

    private fun setMovieAdapter() {
        adapter.callback = object : SingleUnitAdapter.Callback{
            override fun onClickItem(movie: Movie) {
                activity.addFragment(DetailFragment().apply { arguments = Bundle().apply {
                    putInt(Constants.VIDEO_ID, movie.videosId?.toInt()?:0)
                } })
            }

        }
        binding.rcvMovieSingle.adapter = adapter
    }

    private fun setClickListener() {
        binding.btnSort.setOnClickListener {
            val dialogBinding = MenuSortBinding.inflate(layoutInflater)
            val dialog = Dialog(requireContext(), R.style.MyDialog)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(dialogBinding.root)
            dialog.show()
            dialogBinding.btnSortByNewest.setOnClickListener {
                adapter.submitList(adapter.currentList.sortedByDescending { item -> item.videosId })
                dialog.dismiss()
            }
            dialogBinding.btnOldestSort.setOnClickListener {
                adapter.submitList(adapter.currentList.sortedBy { item -> item.videosId })
                dialog.dismiss()
            }
            dialogBinding.btnMaxViewSort.setOnClickListener {
                Toast.makeText(activity, "Tính năng đang được phát triển", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
            dialogBinding.btnPriceSort.setOnClickListener {
                Toast.makeText(activity, "Tính năng đang được phát triển", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }
        }
    }

    private fun getData(pageNo: Int) {
        nextPage = pageNo + 1
        if (pageNo > 1) {
            showProgressbar(true)
        }
        singleViewModel.getListSingleUnit(pageNo)
    }

    private fun observer() {
        singleViewModel.listSingleUnit.observe(activity) {
            if (!it.isNullOrEmpty()) {
                if (nextPage == 1) {
                    adapter.submitList(it)
                } else {
                    val list = ArrayList<Movie>()
                    list.addAll(adapter.currentList)
                    it.forEach { movie ->
                        val exitsMovie = list.none { item -> item.videosId == movie.videosId }
                        if (exitsMovie) {
                            list.add(movie)
                        }
                    }
                    adapter.submitList(list)
                }
                loadMore = true
            } else {
                loadMore = false
            }
            showProgressbar(false)
        }
    }

    private fun setUpLoadMore() {
        scrollListener = object :
            EndlessRecyclerViewScrollListener(binding.rcvMovieSingle.layoutManager as GridLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if (!loadMore) {
                    return
                }
                getData(nextPage)
            }
        }
        binding.rcvMovieSingle.addOnScrollListener(scrollListener)
    }
}