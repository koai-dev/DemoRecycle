package com.idance.hocnhayonline.singleUnit

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentSingleUnitBinding
import com.idance.hocnhayonline.databinding.MenuSortBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleUnitFragment : BaseFragment() {
    private lateinit var binding: FragmentSingleUnitBinding
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
        setClickListener()
    }

    private fun setMovieAdapter() {
    }

    private fun setClickListener() {
        binding.btnSort.setOnClickListener {
            val dialogBinding = MenuSortBinding.inflate(layoutInflater)
            val dialog = Dialog(requireContext(), R.style.MyDialog)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(dialogBinding.root)
            dialog.show()
        }
    }

    private fun genData() {

    }
}