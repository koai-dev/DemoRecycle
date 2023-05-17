package com.idance.hocnhayonline.main.signup

import android.os.Bundle
import android.os.SystemClock
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentSignUpBinding
import com.idance.hocnhayonline.main.MainActivity
import com.idance.hocnhayonline.utils.LoginUtils
import com.koaidev.idancesdk.model.User

class SignUpFragment : BaseFragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var activity: MainActivity
    private var mLastClickTime = 0L

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
            insets
        }
        setClick()
    }

    private fun setClick() {

        binding.btnRegister.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            val name = binding.edtName.text.trim().toString()
            var email = binding.edtNameLogin.text.trim().toString()
            val password = binding.edtPassword.text.trim().toString()
            if (name.isEmpty()) {
                Toast.makeText(activity, "Bạn cần nhập họ tên.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (email.isEmpty()) {
                Toast.makeText(activity, "Bạn cần nhập tên đăng nhập.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                Toast.makeText(activity, "Bạn cần nhập mật khẩu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@")) {
                email = "$email@gmail.com"
            }
            LoginUtils.signUp(activity,
                name,
                email,
                password,
                object : LoginUtils.LoginCallBack {
                    override fun onLoginSuccess(user: User?) {
                        Toast.makeText(activity, "Chào mừng bạn đã đến với IDANCE", Toast.LENGTH_SHORT).show()
                    }

                    override fun onLoginFail(user: User?) {
                        Toast.makeText(activity, "Có lỗi xảy ra. ${user?.message}", Toast.LENGTH_SHORT).show()
                    }

                })
        }
        binding.btnBack.setOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }
}