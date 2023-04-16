package com.idance.hocnhayonline

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivityMainBinding
import com.idance.hocnhayonline.welcome.WelcomeLoginFragment
import com.koaidev.idancesdk.model.Config
import com.koaidev.idancesdk.service.ApiController
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainPagerAdapter: MainPagerAdapter
    var doubleBackToExitPressedOnce = false
    lateinit var callbackManager: CallbackManager


    override fun getBindingView(): ViewBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as ActivityMainBinding
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Toast.makeText(this@MainActivity, "Đăng nhập bị hủy.", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@MainActivity, "Lỗi đã xảy ra.", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(result: LoginResult) {
                    val accessToken = AccessToken.getCurrentAccessToken()
                    if (accessToken != null && !accessToken.isExpired) {
                        Toast.makeText(
                            this@MainActivity,
                            "Đăng nhập thành công.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })

        setMainPager()
        setStateBar(0)
        setUpListener()
    }

    private fun setMainPager() {
        mainPagerAdapter = MainPagerAdapter(this)
        binding.pagerMain.adapter = mainPagerAdapter
        binding.pagerMain.offscreenPageLimit = 5
        binding.pagerMain.isUserInputEnabled = false
        binding.pagerMain.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != 4) {
                    setStateBar(position)
                } else {
                    tabProfileClick()
                }
            }
        })
    }

    private fun setUpListener() {
        binding.layoutBottomTab.btnHomeSuper.setOnClickListener {
            binding.pagerMain.currentItem = 0
        }
        binding.layoutBottomTab.btnSingleUnit.setOnClickListener {
            binding.pagerMain.currentItem = 1
        }
//        binding.layoutBottomTab.btnShort.setOnClickListener {
//            binding.pagerMain.currentItem = 2
//        }
        binding.layoutBottomTab.btnCourse.setOnClickListener {
            binding.pagerMain.currentItem = 2
        }
        binding.layoutBottomTab.btnPersonalSuper.setOnClickListener {
            tabProfileClick()
        }
        binding.btnSupport.setOnClickListener {

        }
        this.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fm = supportFragmentManager
                val count = fm.backStackEntryCount
                if (count > 0) {
                    fm.popBackStack()
                } else {
                    showAlertDialogExitApp()
                }
            }

        })

        ApiController.getService().config().enqueue(object : Callback<Config> {
            override fun onResponse(call: Call<Config>, response: Response<Config>) {
                print(response.body())
            }

            override fun onFailure(call: Call<Config>, t: Throwable) {

            }

        })
    }

    private fun tabProfileClick() {
        if (Firebase.auth.currentUser != null) {
            binding.pagerMain.currentItem = 4
        } else {
            addFragment(WelcomeLoginFragment())
        }
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun clearStack() {
        val itemCount = supportFragmentManager.backStackEntryCount
        for (item in 0 until itemCount) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    private fun setStateBar(index: Int) {
        binding.layoutBottomTab.btnHomeSuper.isSelected = index == 0
        binding.layoutBottomTab.btnSingleUnit.isSelected = index == 1
//        binding.layoutBottomTab.btnShort.isSelected = index == 2
        binding.layoutBottomTab.btnCourse.isSelected = index == 2
        binding.layoutBottomTab.btnPersonalSuper.isSelected = index == 3
        if (index != 3) {
            binding.btnSupport.visibility = View.VISIBLE
        } else {
            binding.btnSupport.visibility = View.GONE
        }
    }

    private fun showAlertDialogExitApp() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show()
        binding.root.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}