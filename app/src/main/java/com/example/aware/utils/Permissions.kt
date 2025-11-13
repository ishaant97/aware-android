package com.example.aware.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings

fun checkNotificationPermission(context: Context) {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    val packageName = context.packageName

    if (!enabledListeners.contains(packageName)) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    }
}