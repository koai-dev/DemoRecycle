package com.idance.hocnhayonline

import androidx.multidex.MultiDexApplication
import com.idance.hocnhayonline.utils.AppConfig
import com.idance.hocnhayonline.utils.AppConfigUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AppConfigUtil.appConfig = AppConfig()

    }
}