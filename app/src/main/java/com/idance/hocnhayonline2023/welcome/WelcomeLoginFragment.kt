package com.idance.hocnhayonline2023.welcome

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline2023.MainActivity
import com.idance.hocnhayonline2023.base.BaseFragment
import com.idance.hocnhayonline2023.databinding.FragmentWelcomeLoginBinding
import com.idance.hocnhayonline2023.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeLoginFragment : BaseFragment() {
    private lateinit var binding: FragmentWelcomeLoginBinding
    override fun getBindingView(): ViewBinding = FragmentWelcomeLoginBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as FragmentWelcomeLoginBinding
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

        setClickListener()
    }

    private fun setClickListener(){
        binding.btnAgree.setOnClickListener {
            (requireActivity() as MainActivity).addFragment(LoginFragment())
        }
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}