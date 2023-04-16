package com.idance.hocnhayonline.base

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.databinding.ActivityFaBinding
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
            exitTransition = Explode()
            enterTransition = Fade()
        }
        faBinding = ActivityFaBinding.inflate(layoutInflater)
        binding = getBindingView()
        setContentView(faBinding.root)
        faBinding.rootContainer.addView(binding?.root)
        if (binding != null) {
            initView(savedInstanceState, binding!!)
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