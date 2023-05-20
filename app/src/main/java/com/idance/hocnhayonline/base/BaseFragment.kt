package com.idance.hocnhayonline.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

open class BaseFragment : FaFragment() {
    override fun getBindingView(): ViewBinding? = null

    override fun getBindingView(container: ViewGroup?): ViewBinding? = null

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
    }
}