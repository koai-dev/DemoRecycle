package com.idance.hocnhayonline.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivitySplashBinding
import com.idance.hocnhayonline.utils.LoginUtils
import java.security.MessageDigest

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun getBindingView(): ViewBinding = ActivitySplashBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        LoginUtils.getUserByUid(this)
        printHashKey(this)
        binding.root.postDelayed({ openActivity(MainActivity::class.java, false) }, 2000)
    }
    fun printHashKey(pContext: Context?) {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e("SplashActivity.TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: Exception) {
            Log.e("SplashActivity.TAG", "printHashKey()", e)
        }
    }
}