package com.example.aware.utils

import android.content.Context
import androidx.core.content.edit

object FirstLaunchHelper {
    private const val PREF_NAME = "app_launch"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    fun isFirstLaunch(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchFlag(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit() {
            putBoolean(KEY_FIRST_LAUNCH, false)
        }
    }
}
