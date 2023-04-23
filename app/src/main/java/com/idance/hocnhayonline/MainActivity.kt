package com.idance.hocnhayonline

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivityMainBinding
import com.idance.hocnhayonline.utils.LoginUtils
import com.idance.hocnhayonline.welcome.WelcomeLoginFragment
import com.koaidev.idancesdk.AccountUtil
import com.koaidev.idancesdk.model.Config
import com.koaidev.idancesdk.model.User
import com.koaidev.idancesdk.service.ApiController
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainPagerAdapter: MainPagerAdapter
    private var doubleBackToExitPressedOnce = false
    lateinit var callbackManager: CallbackManager
    lateinit var loginCallBack: LoginUtils.LoginCallBack
    lateinit var oneTapClient: SignInClient
    private var countLoginClick = 0

    val registerForActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            countLoginClick+=1
            try {
                if (it?.data != null) {
                    val credential = oneTapClient.getSignInCredentialFromIntent(it.data)
                    val idToken = credential.googleIdToken
                    Firebase.auth.signInWithCredential(
                        GoogleAuthProvider.getCredential(
                            idToken,
                            null
                        )
                    )
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = task.result.user?.uid
                                val phone = task.result.user?.phoneNumber
                                val email = task.result.user?.email
                                if (uid != null) {
                                    LoginUtils.authFirebase(this, uid, email, phone, loginCallBack)
                                }
                            } else {
                                loginCallBack.onLoginFail(
                                    User(
                                        message = task.exception?.message ?: "Unknown Error."
                                    )
                                )
                            }
                        }
                }
            } catch (e: ApiException) {
                e.printStackTrace()
                when(e.statusCode){
                    CommonStatusCodes.CANCELED -> if (countLoginClick==2){
                        loginCallBack.onLoginFail(
                            User(
                                message = "Too many action cancel to login."))
                    }
                }
            }
        }


    override fun getBindingView(): ViewBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as ActivityMainBinding
        callbackManager = CallbackManager.Factory.create()
        oneTapClient = Identity.getSignInClient(this)
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Toast.makeText(this@MainActivity, "Đăng nhập bị hủy.", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@MainActivity, "Lỗi đã xảy ra.", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(result: LoginResult) {
                    val accessToken = AccessToken.getCurrentAccessToken()
                    if (accessToken != null && !accessToken.isExpired) {
                        val credential = FacebookAuthProvider.getCredential(accessToken.token)
                        Firebase.auth.currentUser?.linkWithCredential(credential)
                        Firebase.auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result.user?.uid
                                    val phone = task.result.user?.phoneNumber
                                    val email = task.result.user?.email
                                    if (uid != null) {
                                        LoginUtils.authFirebase(
                                            this@MainActivity,
                                            uid,
                                            email,
                                            phone,
                                            loginCallBack
                                        )
                                    }
                                } else if (task.exception?.message?.contains("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.") == true) {
                                    LoginUtils.loginByGoogle(
                                        oneTapClient,
                                        registerForActivityResultLauncher,
                                        loginCallBack
                                    )
                                } else {
                                    loginCallBack.onLoginFail(
                                        User(
                                            message = task.exception?.message ?: "Unknown Error."
                                        )
                                    )
                                }
                            }
                    }
                }

            })

        setMainPager()
        setStateBar(0)
        setUpListener()
        binding.root.post {
            binding.btnSupport.setWidthHeight(binding.pagerMain.measuredHeight)
        }
    }

    private fun setMainPager() {
        mainPagerAdapter = MainPagerAdapter(this)
        binding.pagerMain.adapter = mainPagerAdapter
        binding.pagerMain.offscreenPageLimit = 5
        binding.pagerMain.isUserInputEnabled = false
        binding.pagerMain.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != 4) {
                    setStateBar(position)
                } else {
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
//        binding.layoutBottomTab.btnShort.setOnClickListener {
//            binding.pagerMain.currentItem = 2
//        }
        binding.layoutBottomTab.btnCourse.setOnClickListener {
            binding.pagerMain.currentItem = 2
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

        ApiController.getService().config().enqueue(object : Callback<Config> {
            override fun onResponse(call: Call<Config>, response: Response<Config>) {
                print(response.body())
            }

            override fun onFailure(call: Call<Config>, t: Throwable) {

            }

        })
    }

    private fun tabProfileClick() {
        if (AccountUtil.isLogin()) {
            binding.pagerMain.currentItem = 4
        } else {
            addFragment(WelcomeLoginFragment())
        }
    }

    fun tabHomeClick(){
        binding.pagerMain.currentItem = 0
    }

    fun tabSingleClick(){
        binding.pagerMain.currentItem = 1
    }

    fun tabCourseClick(){
        binding.pagerMain.currentItem = 2
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun clearStack() {
        val itemCount = supportFragmentManager.backStackEntryCount
        for (item in 0 until itemCount) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    private fun setStateBar(index: Int) {
        binding.layoutBottomTab.btnHomeSuper.isSelected = index == 0
        binding.layoutBottomTab.btnSingleUnit.isSelected = index == 1
//        binding.layoutBottomTab.btnShort.isSelected = index == 2
        binding.layoutBottomTab.btnCourse.isSelected = index == 2
        binding.layoutBottomTab.btnPersonalSuper.isSelected = index == 3
        if (index != 3) {
            binding.btnSupport.visibility = View.VISIBLE
        } else {
            binding.btnSupport.visibility = View.GONE
        }
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