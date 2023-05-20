package com.idance.hocnhayonline.play.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayViewModel : ViewModel() {
    val playbackPosition = MutableLiveData<Long>()
    val hasMirror = MutableLiveData<Boolean>()

    init {
        playbackPosition.value = 0L
        hasMirror.value = false
    }
}