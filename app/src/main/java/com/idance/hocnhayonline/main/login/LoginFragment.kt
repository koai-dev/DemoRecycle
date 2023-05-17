package com.idance.hocnhayonline.main.login

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentLoginBinding
import com.idance.hocnhayonline.main.signup.SignUpFragment
import com.idance.hocnhayonline.utils.LoginUtils
import com.koaidev.idancesdk.AccountUtil
import com.koaidev.idancesdk.model.User
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment(), LoginUtils.LoginCallBack {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var activity: MainActivity
    override fun getBindingView(): ViewBinding = FragmentLoginBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentLoginBinding
        activity = requireActivity() as MainActivity
        activity.loginCallBack = this
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

        //init user for begin
        val currentUser = LoginUtils.getOldUserEmailAndPassword(activity)
        currentUser?.email?.let { binding.edtNameLogin.setText(currentUser.email) }
        currentUser?.password?.let { binding.edtPassword.setText(currentUser.password) }
        setClickListener()
    }

    private fun setClickListener() {
        binding.btnBack.setOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
        binding.btnRegister.setOnClickListener {
            activity.addFragment(SignUpFragment())
        }
        binding.layoutFacebookGoogle.btnFacebook.setOnClickListener {
            LoginUtils.loginByFacebook(activity)
        }
        binding.layoutFacebookGoogle.btnGoogle.setOnClickListener {
            LoginUtils.loginByGoogle(
                activity.oneTapClient,
                activity.registerForActivityResultLauncher,
                activity.loginCallBack
            )
        }
        binding.btnLogin.setOnClickListener {
            var email = binding.edtNameLogin.text.trim().toString()
            val password = binding.edtPassword.text.trim().toString()
            if (email.isEmpty()) {
                Toast.makeText(activity, "Bạn cần nhập tên đăng nhập.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                Toast.makeText(activity, "Bạn cần nhập mật khẩu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@")) {
                email = "$email@gmail.com"
            }
            LoginUtils.login(
                activity,
                email = email,
                password = password,
                this
            )
        }
    }

    override fun onLoginSuccess(user: User?) {
        Toast.makeText(
            activity,
            "Chào mừng ${user?.name ?: user?.email} đã đến với IDance",
            Toast.LENGTH_SHORT
        )
            .show()
        activity.clearStack()
        AccountUtil.setUser(user)
    }

    override fun onLoginFail(user: User?) {
        Toast.makeText(activity, user?.message, Toast.LENGTH_SHORT).show()
        AccountUtil.setUser(null)
    }
}