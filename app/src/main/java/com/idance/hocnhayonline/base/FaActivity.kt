package com.idance.hocnhayonline.base

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.databinding.ActivityFaBinding
import com.idance.hocnhayonline.utils.NetworkUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class FaActivity : AppCompatActivity() {
    private var binding: ViewBinding? = null
    private lateinit var faBinding: ActivityFaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            // Set an exit transition
            exitTransition = Slide(Gravity.END)
            enterTransition = Slide(Gravity.START)
        }
        faBinding = ActivityFaBinding.inflate(layoutInflater)
        binding = getBindingView()
        setContentView(faBinding.root)
        faBinding.rootContainer.addView(binding?.root)
        if (binding != null) {
            initView(savedInstanceState, binding!!)
        }
        NetworkUtil(this).observe(this) {
            faBinding.containerNoInternet.visibility = if (it) {
                View.GONE
            } else {
                View.VISIBLE
            }
            faBinding.rootContainer.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        faBinding.btnRetry.setOnClickListener {
            Toast.makeText(this, "Please check your network!", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun getBindingView(): ViewBinding?

    abstract fun initView(savedInstanceState: Bundle?, binding: ViewBinding)

    fun openActivity(activity: Class<*>, canBack: Boolean = true, bundle: Bundle? = null) {
        val intent = Intent(this, activity)
        if (bundle != null) {
            intent.putExtra("bundle", bundle)
        }
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        if (!canBack) {
            finish()
        }
    }

}