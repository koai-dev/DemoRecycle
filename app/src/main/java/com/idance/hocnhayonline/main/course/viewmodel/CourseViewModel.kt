package com.idance.hocnhayonline.main.course.viewmodel

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

class CourseViewModel : ViewModel() {
    val listCourse = MutableLiveData<MutableList<Movie>?>()
    fun getListCourse(pageNo: Int) {
        viewModelScope.launch {
            ApiController.getService().getAllCourse(
                apiKey = AppConfigUtil.appConfig.apiKey,
                authorization = AppConfigUtil.appConfig.authorization,
                pageNo = pageNo.toString()
            ).enqueue(object : Callback<MutableList<Movie>> {
                override fun onResponse(
                    call: Call<MutableList<Movie>>,
                    response: Response<MutableList<Movie>>
                ) {
                    if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                        listCourse.postValue(response.body())
                    } else {
                        listCourse.postValue(null)
                    }
                }

                override fun onFailure(call: Call<MutableList<Movie>>, t: Throwable) {
                    t.printStackTrace()
                }

            })
        }
    }
}