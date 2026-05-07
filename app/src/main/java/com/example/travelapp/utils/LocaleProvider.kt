package com.example.travelapp.utils

import androidx.compose.runtime.*

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
val LocalAppLocale = staticCompositionLocalOf { AppLocale.ENGLISH }

@Composable
fun LocaleProvider(
    locale: AppLocale,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppStrings provides locale.toStrings(),
        LocalAppLocale provides locale
    ) {
        content()
    }
}