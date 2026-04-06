package com.example.travelapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.travelapp.BuildConfig

data class PlaceItem(
    val id: String,
    val name: String,
    val location: String,
    val visitDate: String = ""
)

data class SearchPlacesUiState(
    val routeName: String = "",
    val searchQuery: String = "",
    val places: List<PlaceItem> = emptyList(),
    val suggestions: List<AutocompletePrediction> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

class SearchPlacesViewModel(application: Application) : AndroidViewModel(application) {

    private val placesClient: PlacesClient

    init {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(application, BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(application)
    }

    private val _uiState = MutableStateFlow(SearchPlacesUiState())
    val uiState: StateFlow<SearchPlacesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onRouteNameChange(name: String) {
        _uiState.update { it.copy(routeName = name) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }

        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            if (!isNetworkAvailable()) {
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        isSearching = false,
                        errorMessage = "Немає підключення до інтернету"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSearching = true) }
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .build()

                val response = placesClient
                    .findAutocompletePredictions(request)
                    .await()

                _uiState.update {
                    it.copy(
                        suggestions = response.autocompletePredictions,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) return@launch
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        isSearching = false,
                        errorMessage = "Помилка пошуку: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onSuggestionSelected(prediction: AutocompletePrediction) {
        val newPlace = PlaceItem(
            id = prediction.placeId,
            name = prediction.getPrimaryText(null).toString(),
            location = prediction.getSecondaryText(null).toString()
        )
        _uiState.update {
            it.copy(
                places = it.places + newPlace,
                suggestions = emptyList(),
                searchQuery = ""
            )
        }
    }

    fun removePlace(index: Int) {
        _uiState.update { state ->
            state.copy(places = state.places.toMutableList().also { it.removeAt(index) })
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun movePlace(from: Int, to: Int) {
        _uiState.update { state ->
            val list = state.places.toMutableList()
            list.add(to, list.removeAt(from))
            state.copy(places = list)
        }
    }
    fun updatePlaceDate(index: Int, date: String) {
        _uiState.update { state ->
            val list = state.places.toMutableList()
            list[index] = list[index].copy(visitDate = date)
            state.copy(places = list)
        }
    }
}