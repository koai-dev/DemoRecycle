package com.idance.hocnhayonline.play

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.R
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivityPlayVideoBinding
import com.idance.hocnhayonline.databinding.CustomPlayerControllerBinding
import com.idance.hocnhayonline.play.viewmodel.PlayViewModel
import com.idance.hocnhayonline.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayVideoActivity : BaseActivity() {
    private lateinit var binding: ActivityPlayVideoBinding
    private lateinit var controllerBinding: CustomPlayerControllerBinding
    private lateinit var player: ExoPlayer
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    @Inject
    lateinit var playViewModel: PlayViewModel
    private var urlVideo: String? = null
    private var mLastClickTime = 0L

    override fun getBindingView(): ViewBinding =
        DataBindingUtil.inflate(layoutInflater, R.layout.activity_play_video, null, false)

    @SuppressLint("CutPasteId")
    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as ActivityPlayVideoBinding
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val paramsTop =
                findViewById<TextView>(R.id.point_top).layoutParams as ViewGroup.MarginLayoutParams
            paramsTop.setMargins(
                0,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                0,
                0
            )
            findViewById<TextView>(R.id.point_top).layoutParams = paramsTop
            insets.consumeSystemWindowInsets()
        }
        controllerBinding = CustomPlayerControllerBinding.inflate(layoutInflater)
        urlVideo = intent.getBundleExtra(Constants.BUNDLE)?.getString(Constants.VIDEO_URL)
        observer()
        setClick()

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initPlayer(url: String, hasMirror: Boolean) {
        player = ExoPlayer.Builder(this).build().also {
            if (hasMirror) {
                binding.videoViewMirror.setPlayer(it)
            } else {
                binding.videoView.setPlayer(it)
            }
            val mediaItem =
                MediaItem.fromUri(url)
            it.setMediaItem(mediaItem)
            it.playWhenReady = playWhenReady
            it.seekTo(currentItem, playbackPosition)
            it.prepare()
        }
    }

    private fun observer() {
        playViewModel.playbackPosition.observe(this) {
            playbackPosition = it
        }
        playViewModel.hasMirror.observe(this) {
            binding.hasMirroring = it
            initPlayer(urlVideo!!, it)
        }
    }

    private fun setClick() {
       findViewById<ImageButton>(R.id.btn_mirror).setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            releasePlayer()
            playViewModel.hasMirror.postValue(!playViewModel.hasMirror.value!!)
        }
        binding.videoViewMirror.setOnClickListener {
            binding.hasShowMirror = binding.videoViewMirror.controller?.isFullyVisible
        }
        binding.videoView.setOnClickListener {
            binding.hasShowMirror = binding.videoView.controller?.isFullyVisible
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playViewModel.playbackPosition.postValue(exoPlayer.currentPosition)
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
    }


    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}