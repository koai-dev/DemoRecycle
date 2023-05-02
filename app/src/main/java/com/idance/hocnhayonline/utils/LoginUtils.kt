package com.idance.hocnhayonline.utils

import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.idance.hocnhayonline.main.MainActivity
import com.koaidev.idancesdk.AccountUtil
import com.koaidev.idancesdk.model.User
import com.koaidev.idancesdk.service.ApiController
import com.koaidev.idancesdk.utils.Const
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LoginUtils {
    fun getUserByUid(context: Context): User? {
        val uid = SharePreference.getStringPref(context, Constants.PARAM_UID)
        return if (uid != null) {
            User(uid = uid)
        } else {
            null
        }
    }

    fun getOldUserEmailAndPassword(context: Context): User? {
        val email = SharePreference.getStringPref(context, Constants.PARAM_EMAIL)
        val password = SharePreference.getStringPref(context, Constants.PARAM_PASSWORD)
        return if (email != null && password != null) {
            User(email = email, passwordAvailable = true, password = password)
        } else {
            null
        }
    }

    fun signUp(
        context: Context,
        name: String,
        email: String,
        password: String,
        loginCallBack: LoginCallBack
    ) {
        val fields = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(Constants.PARAM_NAME, name)
            .addFormDataPart(Constants.PARAM_EMAIL, email)
            .addFormDataPart(Constants.PARAM_PASSWORD, password)

        ApiController.getService().signup(
            apiKey = AppConfigUtil.appConfig.apiKey,
            authorization = AppConfigUtil.appConfig.authorization,
            fields.build()
        ).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    saveUserEmailAndPassword(context, email, password)
                    loginCallBack.onLoginSuccess(response.body())
                } else {
                    loginCallBack.onLoginFail(response.body())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
                loginCallBack.onLoginFail(User(message = t.message))
            }

        })
    }

    fun login(context: Context, email: String, password: String, loginCallBack: LoginCallBack) {
        val fields = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(Constants.PARAM_EMAIL, email)
            .addFormDataPart(Constants.PARAM_PASSWORD, password)
        ApiController.getService().login(
            apiKey = AppConfigUtil.appConfig.apiKey,
            authorization = AppConfigUtil.appConfig.authorization,
            fields.build()
        ).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    saveUserEmailAndPassword(context, email, password)
                    loginCallBack.onLoginSuccess(response.body())
                } else {
                    loginCallBack.onLoginFail(response.body())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
                loginCallBack.onLoginFail(User(message = t.message))
            }

        })
    }

    fun authFirebase(
        context: Context, uid: String, email: String?, phone: String?, loginCallBack: LoginCallBack
    ) {
        val fields = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(Constants.PARAM_EMAIL, email ?: "")
            .addFormDataPart(Constants.PARAM_UID, uid)
            .addFormDataPart(Constants.PARAM_PHONE, phone ?: "")
        ApiController.getService().firebaseAuth(
            apiKey = AppConfigUtil.appConfig.apiKey,
            authorization = AppConfigUtil.appConfig.authorization,
            requestBody = fields.build()
        ).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    saveUserUid(context, uid)
                    loginCallBack.onLoginSuccess(response.body())
                } else {
                    loginCallBack.onLoginFail(response.body())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
                loginCallBack.onLoginFail(User(message = t.message))
            }

        })
    }

    fun loginByFacebook(activity: MainActivity) {
        LoginManager.getInstance().logInWithReadPermissions(
            activity, activity.callbackManager, listOf("public_profile", "email")
        )
    }

    fun loginByGoogle(
        oneTapClient: SignInClient,
        registerForActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        loginCallBack: LoginCallBack
    ) {
        val signInRequest = BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                    .setServerClientId(Const.SERVER_CLIENT_ID).setFilterByAuthorizedAccounts(false)
                    .build()
            ).setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
            ).setAutoSelectEnabled(false).build()

        oneTapClient.beginSignIn(signInRequest).addOnCompleteListener {
            if (it.isSuccessful) {
                val intentSenderRequest =
                    IntentSenderRequest.Builder(it.result.pendingIntent.intentSender)
                        .setFlags(FLAG_IMMUTABLE, FLAG_IMMUTABLE).build()
                registerForActivityResultLauncher.launch(intentSenderRequest)
            } else if (it.isCanceled) {
                loginCallBack.onLoginFail(User(message = "Canceled login by google"))
            } else {
                loginCallBack.onLoginFail(User(message = it.exception?.message))
            }
        }

    }

    fun logout(context: Context, logoutCallBack: LogoutCallBack) {
        saveUserEmailAndPassword(context, null, null)
        saveUserUid(context, null)
        AccountUtil.setUser(null)
        Firebase.auth.signOut()
        logoutCallBack.onLogoutDone()
    }

    private fun saveUserEmailAndPassword(context: Context, email: String?, password: String?) {
        SharePreference.setStringPref(context, Constants.PARAM_EMAIL, email)
        SharePreference.setStringPref(context, Constants.PARAM_PASSWORD, password)
    }

    fun saveUserUid(context: Context, uid: String?) {
        SharePreference.setStringPref(context, Constants.PARAM_UID, uid)
    }

    interface LoginCallBack {
        fun onLoginSuccess(user: User?)
        fun onLoginFail(user: User?)
    }

    interface LogoutCallBack {
        fun onLogoutDone()
    }
}