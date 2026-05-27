package com.example.travelapp.notification

import android.content.Context
import androidx.core.content.edit

object NotificationPrefs {
    private const val PREFS_NAME = "notification_settings"
    private const val KEY_ENABLED = "notifications_enabled"

    fun areEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_ENABLED, enabled) }
    }
}