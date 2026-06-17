package com.example.travelapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.example.travelapp.viewmodel.create.SearchError

@Composable
@ReadOnlyComposable
fun SearchError.toMessage(): String {
    val strings = LocalAppStrings.current
    return when (this) {
        SearchError.NO_INTERNET -> strings.noInternetError
        SearchError.INVALID_REQUEST -> strings.hotelSearchError
    }
}