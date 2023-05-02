package com.idance.hocnhayonline.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivitySplashBinding
import com.idance.hocnhayonline.utils.LoginUtils
import com.koaidev.idancesdk.AccountUtil
import com.koaidev.idancesdk.model.User

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun getBindingView(): ViewBinding = ActivitySplashBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        val user = LoginUtils.getUserByUid(this) ?: LoginUtils.getOldUserEmailAndPassword(this)
        if (user != null) {
            if (user.uid != null) {
                LoginUtils.authFirebase(
                    this,
                    user.uid!!,
                    "",
                    "",
                    object : LoginUtils.LoginCallBack {
                        override fun onLoginSuccess(user: User?) {
                            AccountUtil.setUser(user!!)
                            binding.root.postDelayed({
                                openActivity(
                                    MainActivity::class.java,
                                    false
                                )
                            }, 2000)
                        }

                        override fun onLoginFail(user: User?) {
                            AccountUtil.setUser(null)
                            binding.root.postDelayed({
                                openActivity(
                                    MainActivity::class.java,
                                    false
                                )
                            }, 2000)
                        }

                    })
            } else {
                LoginUtils.login(
                    this,
                    user.email!!,
                    user.password!!,
                    object : LoginUtils.LoginCallBack {
                        override fun onLoginSuccess(user: User?) {
                            AccountUtil.setUser(user!!)
                            binding.root.postDelayed({
                                openActivity(
                                    MainActivity::class.java,
                                    false
                                )
                            }, 2000)
                        }

                        override fun onLoginFail(user: User?) {
                            AccountUtil.setUser(null)
                            binding.root.postDelayed({
                                openActivity(
                                    MainActivity::class.java,
                                    false
                                )
                            }, 2000)

                        }

                    })
            }
        }else{
            AccountUtil.setUser(null)
            binding.root.postDelayed({
                openActivity(
                    MainActivity::class.java,
                    false
                )
            }, 2000)
        }
    }
}