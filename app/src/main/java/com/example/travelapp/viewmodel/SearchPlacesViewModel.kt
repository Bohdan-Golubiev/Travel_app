package com.example.travelapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaceItem(
    val id: String,
    val name: String,
    val location: String
)

data class SearchPlacesUiState(
    val routeName: String = "",
    val searchQuery: String = "",
    val places: List<PlaceItem> = emptyList()
)

class SearchPlacesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchPlacesUiState())
    val uiState: StateFlow<SearchPlacesUiState> = _uiState.asStateFlow()

    fun onRouteNameChange(name: String) {
        _uiState.update { it.copy(routeName = name) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun removePlace(index: Int) {
        _uiState.update { state ->
            state.copy(places = state.places.toMutableList().also { it.removeAt(index) })
        }
    }

    fun setPlaces(places: List<PlaceItem>) {
        _uiState.update { it.copy(places = places) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }
}