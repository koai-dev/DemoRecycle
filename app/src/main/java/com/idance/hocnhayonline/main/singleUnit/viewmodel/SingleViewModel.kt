package com.idance.hocnhayonline.main.singleUnit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idance.hocnhayonline.utils.AppConfigUtil
import com.koaidev.idancesdk.model.Movie
import com.koaidev.idancesdk.service.ApiController
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class SingleViewModel @Inject constructor() : ViewModel() {
    val listSingleUnit = MutableLiveData<MutableList<Movie>?>()
    fun getListSingleUnit(pageNo: Int) {
        viewModelScope.launch {
            ApiController.getService().getAllMovies(
                apiKey = AppConfigUtil.appConfig.apiKey,
                authorization = AppConfigUtil.appConfig.authorization,
                pageNo.toString()
            ).enqueue(object : Callback<MutableList<Movie>>{
                override fun onResponse(
                    call: Call<MutableList<Movie>>,
                    response: Response<MutableList<Movie>>
                ) {
                    if (response.isSuccessful && response.body()?.isNotEmpty()==true){
                        listSingleUnit.postValue(response.body())
                    }else{
                        listSingleUnit.postValue(null)
                    }
                }

                override fun onFailure(call: Call<MutableList<Movie>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }
    }
}