package com.example.travelapp.viewmodel.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.repository.AirportRepository
import com.example.travelapp.data.repository.AirTransportRepository
import com.example.travelapp.model.dataclasses.Airport
import com.example.travelapp.view.create.BookingOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.db.TravelDB

data class FindVehicleUiState(
    val selectedTransport: String? = null,
    val startPlace: String = "",
    val endPlace: String = "",
    val startSuggestions: List<Airport> = emptyList(),
    val endSuggestions: List<Airport> = emptyList(),
    val selectedStartAirport: Airport? = null,
    val selectedEndAirport: Airport? = null,
    val results: List<BookingOption> = emptyList(),
    val selectedServices: List<BookingOption> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FindVehicleViewModel(
    private val airTransportRepository: AirTransportRepository = AirTransportRepository(),
    private val airportRepository: AirportRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindVehicleUiState())
    val uiState: StateFlow<FindVehicleUiState> = _uiState.asStateFlow()

    fun onTransportSelected(transport: String) {
        _uiState.update { it.copy(selectedTransport = transport) }
    }

    fun onStartPlaceChange(value: String) {
        _uiState.update {
            it.copy(
                startPlace = value,
                startSuggestions = airportRepository.searchByCity(value),
                selectedStartAirport = null
            )
        }
    }

    fun onEndPlaceChange(value: String) {
        _uiState.update {
            it.copy(
                endPlace = value,
                endSuggestions = airportRepository.searchByCity(value),
                selectedEndAirport = null
            )
        }
    }
    fun onStartAirportSelected(airport: Airport) {
        _uiState.update {
            it.copy(
                startPlace = "${airport.city} (${airport.iata})",
                startSuggestions = emptyList(),
                selectedStartAirport = airport
            )
        }
    }

    fun onEndAirportSelected(airport: Airport) {
        _uiState.update {
            it.copy(
                endPlace = "${airport.city} (${airport.iata})",
                endSuggestions = emptyList(),
                selectedEndAirport = airport
            )
        }
    }

    fun onSearchClick() {
        val state = _uiState.value
        val from = state.selectedStartAirport?.iata ?: return
        val to   = state.selectedEndAirport?.iata   ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = airTransportRepository.searchFlights(from, to)
                _uiState.update { it.copy(results = results, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FindVehicleViewModel(
                    airTransportRepository = AirTransportRepository(),
                    airportRepository      = AirportRepository(context),
                    bookingRepository      = BookingRepository(
                        db      = TravelDB.getInstance(context),
                        context = context
                    )
                )
            }
        }
    }

    fun onNextClick(
        userId: String,
        routeId: String,
        onDone: () -> Unit
    ) {
        val services = _uiState.value.selectedServices
        if (services.isEmpty()) {
            onDone()
            return
        }

        viewModelScope.launch {
            val bookings = services.map { option ->
                option.toBookingEntity(
                    userId  = userId,
                    routeId = routeId,
                    type    = when (_uiState.value.selectedTransport) {
                        "Plane" -> "Pl"
                        "Train" -> "Tr"
                        "Bus"   -> "Bs"
                        else    -> "Pl"
                    },
                    from = option.from,
                    to = option.to
                )
            }
            bookingRepository.saveBookings(bookings, userId)
            onDone()
        }
    }

    fun BookingOption.toBookingEntity(
        userId: String,
        routeId: String,
        type: String,
        from: String,
        to: String,
    ): BookingEntity {
        val times = time.split("→").map { it.trim() }
        val flightCode = name.substringAfterLast("·").trim()

        return BookingEntity(
            id            = "${type}_${flightCode}_${date}_${routeId}".uppercase()
                .replace(" ", "")
                .replace("·", ""),
            userId        = userId,
            routeId       = routeId,
            type          = type,
            name          = name,
            departureTime = times.getOrElse(0) { "" },
            arrivalTime   = times.getOrElse(1) { "" },
            date          = date,
            from          = from,
            to            = to,
            createdAt     = System.currentTimeMillis(),
            cost          = 100.0,
            status        = cost,
            isSynced      = false
        )
    }

    fun onAddClick(option: BookingOption) {
        _uiState.update { currentState ->
            val alreadyAdded = currentState.selectedServices.any { it.name == option.name &&
                    it.time == option.time &&
                    it.date == option.date }
            if (alreadyAdded) {
                currentState.copy(
                    selectedServices = currentState.selectedServices - option
                )
            } else {
                currentState.copy(
                    selectedServices = currentState.selectedServices + option
                )
            }
        }
    }
}