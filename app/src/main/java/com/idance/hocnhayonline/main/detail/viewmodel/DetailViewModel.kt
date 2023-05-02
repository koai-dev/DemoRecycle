package com.idance.hocnhayonline.main.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idance.hocnhayonline.utils.AppConfigUtil
import com.koaidev.idancesdk.model.SingleDetailsMovie
import com.koaidev.idancesdk.service.ApiController
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class DetailViewModel @Inject constructor() : ViewModel() {
    val detail = MutableLiveData<SingleDetailsMovie?>()
    fun getDetail(videoId: String, userId: String?) {
        viewModelScope.launch {
            ApiController.getService().getSingleDetailsMovie(
                apiKey = AppConfigUtil.appConfig.apiKey,
                authorization = AppConfigUtil.appConfig.authorization,
                videoId,
                userId
            ).enqueue(object : Callback<SingleDetailsMovie> {
                override fun onResponse(
                    call: Call<SingleDetailsMovie>,
                    response: Response<SingleDetailsMovie>
                ) {
                    if (response.isSuccessful) {
                        detail.postValue(response.body())
                    } else {
                        detail.postValue(null)
                    }
                }

                override fun onFailure(call: Call<SingleDetailsMovie>, t: Throwable) {
                    detail.postValue(null)
                }

            })
        }
    }
}