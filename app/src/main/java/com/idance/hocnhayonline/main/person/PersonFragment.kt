package com.idance.hocnhayonline.main.person

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentPersonalBinding
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.utils.LoginUtils
import com.koaidev.idancesdk.AccountUtil

class PersonFragment : BaseFragment() {
    private lateinit var binding: FragmentPersonalBinding
    private lateinit var activity: MainActivity
    override fun getBindingView(): ViewBinding = FragmentPersonalBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentPersonalBinding
        binding.user = AccountUtil.getUser()
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
            insets
        }

        activity = requireActivity() as MainActivity

        setClickListener()
    }

    private fun setClickListener() {
        binding.btnExit.setOnClickListener {
            LoginUtils.logout(activity, object : LoginUtils.LogoutCallBack {
                override fun onLogoutDone() {
                    Toast.makeText(activity, "Logout!", Toast.LENGTH_SHORT).show()
                    activity.tabHomeClick()
                }

            })
        }
    }

}