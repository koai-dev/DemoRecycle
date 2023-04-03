package com.idance.hocnhayonline2023

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.idance.hocnhayonline2023.base.BaseActivity
import com.idance.hocnhayonline2023.databinding.ActivityMainBinding
import com.idance.hocnhayonline2023.welcome.WelcomeLoginFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainPagerAdapter: MainPagerAdapter
    var doubleBackToExitPressedOnce = false

    override fun getBindingView(): ViewBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as ActivityMainBinding

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
                if (position!=4){
                    setStateBar(position)
                }else{
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
        binding.layoutBottomTab.btnShort.setOnClickListener {
            binding.pagerMain.currentItem = 2
        }
        binding.layoutBottomTab.btnCourse.setOnClickListener {
            binding.pagerMain.currentItem = 3
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

    private fun setStateBar(index: Int) {
        binding.layoutBottomTab.btnHomeSuper.isSelected = index == 0
        binding.layoutBottomTab.btnSingleUnit.isSelected = index == 1
        binding.layoutBottomTab.btnShort.isSelected = index == 2
        binding.layoutBottomTab.btnCourse.isSelected = index == 3
        binding.layoutBottomTab.btnPersonalSuper.isSelected = index == 4
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