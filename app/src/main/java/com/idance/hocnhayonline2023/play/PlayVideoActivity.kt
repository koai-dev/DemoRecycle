package com.idance.hocnhayonline2023.play

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.viewbinding.ViewBinding
import com.idance.hocnhayonline2023.base.BaseActivity
import com.idance.hocnhayonline2023.databinding.ActivityPlayVideoBinding
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
            binding.videoView.player = player
            val mediaItem = MediaItem.fromUri("https://www.youtube.com/watch?v=H5v3kku4y6Q")
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