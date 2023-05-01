package com.idance.hocnhayonline

import android.app.Application
import com.idance.hocnhayonline.utils.AppConfig
import com.idance.hocnhayonline.utils.AppConfigUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppConfigUtil.appConfig = AppConfig()
    }
}