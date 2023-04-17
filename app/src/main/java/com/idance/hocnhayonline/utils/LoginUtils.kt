package com.idance.hocnhayonline.utils

import android.content.Context
import com.idance.hocnhayonline.MainActivity
import com.koaidev.idancesdk.model.User
import com.koaidev.idancesdk.service.ApiController
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

object LoginUtils {
    fun getUserByUid(context: Context): User {
        val uid = SharePreference.getStringPref(context, Constants.PARAM_UID)
        return User(uid = uid)
    }

    fun signUp(name: String, email: String, password: String, loginCallBack: LoginCallBack) {
        val fields = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(Constants.PARAM_NAME, name)
            .addFormDataPart(Constants.PARAM_EMAIL, email)
            .addFormDataPart(Constants.PARAM_PASSWORD, password)

        ApiController.getService().signup(fields.build()).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body()?.status=="success"){
                    loginCallBack.onLoginSuccess(response.body()!!)
                }else{
                    loginCallBack.onLoginFail(response.body())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    interface LoginCallBack{
        fun onLoginSuccess(user: User?)
        fun onLoginFail(user: User?)
    }
}