package com.idance.hocnhayonline.play.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import javax.inject.Singleton

class PlayViewModel @Inject constructor() : ViewModel() {
    val playbackPosition = MutableLiveData<Long>()
    val hasMirror = MutableLiveData<Boolean>()
    init {
        playbackPosition.value = 0L
        hasMirror.value = false
    }
}