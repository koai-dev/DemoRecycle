package com.idance.hocnhayonline.home.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idance.hocnhayonline.utils.AppConfigUtil
import com.koaidev.idancesdk.model.AllCountryItem
import com.koaidev.idancesdk.model.AllGenreItem
import com.koaidev.idancesdk.model.HomeContent
import com.koaidev.idancesdk.model.LatestMoviesItem
import com.koaidev.idancesdk.model.LatestTvseriesItem
import com.koaidev.idancesdk.model.PopularStarsItem
import com.koaidev.idancesdk.model.SlideItem
import com.koaidev.idancesdk.service.ApiController
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class HomeViewModel @Inject constructor() : ViewModel() {
    val showFullListSingle = MutableLiveData<Boolean>()
    val listSlide = MutableLiveData<List<SlideItem?>?>()
    val listLastSingleUnit = MutableLiveData<List<LatestMoviesItem?>?>()
    val listLastSeries = MutableLiveData<List<LatestTvseriesItem?>?>()
    val listLevel = MutableLiveData<List<AllCountryItem?>?>()
    val listCategory = MutableLiveData<List<AllGenreItem?>?>()
    val listTeacher = MutableLiveData<List<PopularStarsItem?>?>()

    init {
        showFullListSingle.value = false
    }

    fun getHomeContent() {
        viewModelScope.launch {
            ApiController.getService()
                .getHomeContent(
                    apiKey = AppConfigUtil.appConfig.apiKey,
                    authorization = AppConfigUtil.appConfig.authorization
                )
                .enqueue(object : Callback<HomeContent> {
                    override fun onResponse(
                        call: Call<HomeContent>,
                        response: Response<HomeContent>
                    ) {
                        if (response.isSuccessful) {
                            listSlide.postValue(response.body()?.slider?.slide)
                            listLastSingleUnit.postValue(response.body()?.latestMovies)
                            listLevel.postValue(response.body()?.allCountry)
                            listCategory.postValue(response.body()?.allGenre)
                            listTeacher.postValue(response.body()?.popularStars)
                            listLastSeries.postValue(response.body()?.latestTvseries)
                        }
                    }

                    override fun onFailure(call: Call<HomeContent>, t: Throwable) {

                    }

                })
        }
    }
}