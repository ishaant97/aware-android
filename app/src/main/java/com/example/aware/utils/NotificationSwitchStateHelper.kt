package com.example.aware.utils

import android.content.Context
import androidx.core.content.edit

object NotificationSwitchStateHelper {
    private const val PREF_NAME = "switch_prefs"
    private const val KEY_NOTIFICATION_SWITCH = "notification_switch_state"

    fun saveSwitchState(context: Context, isChecked: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_NOTIFICATION_SWITCH, isChecked)
        }
    }

    fun getSwitchState(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_SWITCH, false) // default is OFF
    }
}