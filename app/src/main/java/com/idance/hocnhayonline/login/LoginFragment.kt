package com.idance.hocnhayonline.login

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.facebook.login.LoginManager
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentLoginBinding
import com.idance.hocnhayonline.signup.SignUpFragment
import com.idance.hocnhayonline.utils.Constants
import com.idance.hocnhayonline.utils.SharePreference
import com.koaidev.idancesdk.model.User
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var activity: MainActivity
    override fun getBindingView(): ViewBinding = FragmentLoginBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentLoginBinding
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

        //init user for begin
        val currentUser = getOldUserEmailAndPassword()
        currentUser.email?.let { binding.edtEmail.setText(currentUser.email) }
        currentUser.password?.let { binding.edtPassword.setText(currentUser.password) }

        setClickListener()
    }

    private fun saveUserEmailAndPassword(email: String?, password: String?) {
        SharePreference.setStringPref(activity, Constants.PARAM_EMAIL, email)
        SharePreference.setStringPref(activity, Constants.PARAM_PASSWORD, password)
    }

    private fun getOldUserEmailAndPassword(): User {
        val email = SharePreference.getStringPref(activity, Constants.PARAM_EMAIL)
        val password = SharePreference.getStringPref(activity, Constants.PARAM_PASSWORD)
        return User(email = email, passwordAvailable = true, password = password)
    }

    private fun setClickListener() {
        binding.btnBack.setOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
        binding.btnRegister.setOnClickListener {
            activity.addFragment(SignUpFragment())
        }
        binding.layoutFacebookGoogle.btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                activity,
                activity.callbackManager,
                listOf("public_profile", "email")
            )
        }
    }
}