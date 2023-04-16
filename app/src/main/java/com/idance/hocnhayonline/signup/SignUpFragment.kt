package com.idance.hocnhayonline.signup

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.MainActivity
import com.idance.hocnhayonline.base.BaseFragment
import com.idance.hocnhayonline.databinding.FragmentSignUpBinding
import com.idance.hocnhayonline.utils.Constants
import com.koaidev.idancesdk.model.User
import com.koaidev.idancesdk.service.ApiController
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private fun setClick() {
        binding.btnRegister.setOnClickListener {
            signUp("Khánh", "dtakotesstaa12@gmail.com", "12345")
        }
        binding.btnBack.setOnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        val fields = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(Constants.PARAM_NAME, name)
            .addFormDataPart(Constants.PARAM_EMAIL, email)
            .addFormDataPart(Constants.PARAM_PASSWORD, password)

        ApiController.getService().signup(fields.build()).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                   activity.clearStack()
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }
}