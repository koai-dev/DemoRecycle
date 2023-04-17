package com.idance.hocnhayonline.signup

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentSignUpBinding
import com.idance.hocnhayonline.utils.Constants
import com.idance.hocnhayonline.utils.LoginUtils
import com.idance.hocnhayonline.utils.SharePreference
import com.koaidev.idancesdk.model.User

class SignUpFragment : BaseFragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var activity: MainActivity
    override fun getBindingView(): ViewBinding = FragmentSignUpBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentSignUpBinding
        activity = requireActivity() as MainActivity
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
        setClick()
    }

    private fun saveUserEmailAndPassword(email: String?, password: String?) {
        SharePreference.setStringPref(activity, Constants.PARAM_EMAIL, email)
        SharePreference.setStringPref(activity, Constants.PARAM_PASSWORD, password)
    }

    private fun setClick() {
        binding.btnRegister.setOnClickListener {
            LoginUtils.signUp(
                "Khánh",
                "dtakotesstaa12@gmail.com",
                "12345",
                object : LoginUtils.LoginCallBack {
                    override fun onLoginSuccess(user: User?) {

                    }

                    override fun onLoginFail(user: User?) {

                    }

                })
        }
        binding.btnBack.setOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }
}