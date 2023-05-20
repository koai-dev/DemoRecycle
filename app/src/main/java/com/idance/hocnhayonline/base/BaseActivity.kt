package com.idance.hocnhayonline.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.MobileAds

open class BaseActivity : FaActivity() {
    override fun getBindingView(): ViewBinding? = null

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        MobileAds.initialize(this) {}
    }
}