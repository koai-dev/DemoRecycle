package com.idance.hocnhayonline.search.result

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentResultBinding

class ResultFragment : BaseFragment() {
    companion object {
        const val TYPE_SINGLE = 0
        const val TYPE_SERIES = 1
        const val TYPE_TEACHER = 2
    }

    var type = 0
    private lateinit var binding: FragmentResultBinding

    override fun getBindingView(): ViewBinding = FragmentResultBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentResultBinding

    }
}