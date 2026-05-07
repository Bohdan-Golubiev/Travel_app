package com.example.travelapp.utils


import android.content.Context

enum class AppLocale(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    UKRAINIAN("uk", "Українська")
}

object LocaleManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LOCALE = "app_locale"

    fun getSavedLocale(context: Context): AppLocale {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LOCALE, AppLocale.ENGLISH.code)
        return AppLocale.entries.find { it.code == code } ?: AppLocale.ENGLISH
    }

    fun saveLocale(context: Context, locale: AppLocale) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOCALE, locale.code)
            .apply()
    }
}