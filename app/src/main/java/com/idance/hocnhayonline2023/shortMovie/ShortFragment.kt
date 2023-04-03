package com.idance.hocnhayonline2023.shortMovie

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline2023.base.BaseFragment
import com.idance.hocnhayonline2023.databinding.FragmentHomeBinding
import com.idance.hocnhayonline2023.databinding.FragmentShortBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShortFragment : BaseFragment() {
    private lateinit var binding: FragmentShortBinding
    override fun getBindingView(): ViewBinding = FragmentShortBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentShortBinding
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
    }
}