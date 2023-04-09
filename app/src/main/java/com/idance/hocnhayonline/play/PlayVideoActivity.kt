package com.idance.hocnhayonline.play

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline.base.BaseActivity
import com.idance.hocnhayonline.databinding.ActivityPlayVideoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayVideoActivity : BaseActivity() {
    private lateinit var binding: ActivityPlayVideoBinding
    private lateinit var player: ExoPlayer
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    override fun getBindingView(): ViewBinding = ActivityPlayVideoBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?, binding: ViewBinding) {
        super.initView(savedInstanceState, binding)
        this.binding = binding as ActivityPlayVideoBinding

        initPlayer()
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().also {
            binding.videoView.player = it
            val mediaItem = MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/testvideo-d2076.appspot.com/o/TH%C6%AF%C6%A0NG%20EM%20H%C6%A0N%20CH%C3%8DNH%20ANH%20%5BOFFICIAL%20MV%20FULL%5D%20-%20JUN%20PH%E1%BA%A0M.mp4?alt=media&token=9399a62f-4fbf-4a8c-8fd9-f23257eda5ee")
            it.setMediaItem(mediaItem)
            it.playWhenReady = playWhenReady
            it.seekTo(currentItem, playbackPosition)
            it.prepare()
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