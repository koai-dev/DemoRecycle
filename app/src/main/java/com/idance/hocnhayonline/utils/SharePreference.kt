package com.idance.hocnhayonline.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharePreference {
    companion object {
        const val CURRENT_LANGUAGE: String = "current_language"
        const val CURRENT_THEME: String = "current_theme"
        const val IS_VIP = "is_vip"
        private const val PREF_NAME: String = "com.idance.hocnhayonline"


        fun getIntPref(context: Context, key: String): Int {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getInt(key, -1)
        }

        fun setIntPref(context: Context, key: String, value: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val pref: SharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                pref.edit().putInt(key, value).apply()
            }
        }

        fun getStringPref(context: Context, key: String): String? {
            val pref: SharedPreferences? =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref!!.getString(key, null)
        }

        fun setStringPref(context: Context, key: String, value: String?) {
            CoroutineScope(Dispatchers.IO).launch {
                val pref: SharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                pref.edit().putString(key, value).apply()
            }
        }

        fun getBooleanPref(context: Context, key: String): Boolean {

            val pref: SharedPreferences? =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            return pref!!.getBoolean(key, false)
        }

        fun setBooleanPref(context: Context, key: String, value: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                val pref: SharedPreferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                pref.edit().putBoolean(key, value).apply()
            }
        }
    }

}