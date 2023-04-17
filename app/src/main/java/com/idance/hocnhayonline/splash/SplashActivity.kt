package com.idance.hocnhayonline.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivitySplashBinding
import com.idance.hocnhayonline.utils.Constants
import com.idance.hocnhayonline.utils.LoginUtils
import com.idance.hocnhayonline.utils.SharePreference
import com.koaidev.idancesdk.AccountUtil
import com.koaidev.idancesdk.model.User

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun getBindingView(): ViewBinding = ActivitySplashBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        LoginUtils.getUserByUid(this)
        binding.root.postDelayed({ openActivity(MainActivity::class.java, false) }, 2000)
    }

}