package com.example.aware.utils

import android.content.Context

fun getStatusBarHeight(context: Context): Int {
    var statusBarHeight = 0
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}
