package com.example.travelapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travelapp.view.create.BookingOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FindVehicleUiState(
    val selectedTransport: String? = null,
    val startPlace: String = "",
    val endPlace: String = ""
)

class FindVehicleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FindVehicleUiState())
    val uiState: StateFlow<FindVehicleUiState> = _uiState.asStateFlow()

    fun onTransportSelected(transport: String) {
        _uiState.update { it.copy(selectedTransport = transport) }
    }

    fun onStartPlaceChange(value: String) {
        _uiState.update { it.copy(startPlace = value) }
    }

    fun onEndPlaceChange(value: String) {
        _uiState.update { it.copy(endPlace = value) }
    }

    fun onAddClick(option: BookingOption) {
        // TODO
    }
}