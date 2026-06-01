package com.example.travelapp.viewmodel.create

import android.annotation.SuppressLint
import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

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

class FindVehicleViewModel(application: Application) : AndroidViewModel(application) {

    private val airTransportRepository = AirTransportRepository()
    private val airportRepository = AirportRepository(application)
    private val bookingRepository = BookingRepository(TravelDB.getInstance(application), application)
    private val _uiState = MutableStateFlow(FindVehicleUiState())
    val uiState: StateFlow<FindVehicleUiState> = _uiState.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val departureDateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
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
                    type    =  "Pl",
                    from = option.from,
                    to = option.to
                )
            }
            bookingRepository.saveBookings(bookings, userId)

            bookings.forEach { booking ->
                scheduleTransportReminder(booking)
            }

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
            id            = "${type}_${flightCode}_${System.currentTimeMillis()}_${routeId}".uppercase()
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
            cost          = cost,
            status        = status,
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

    private fun scheduleTransportReminder(booking: BookingEntity) {
        val departureMs = parseDepartureMs(booking.date, booking.departureTime)
        if (departureMs == null) {
            Log.w("FindVehicleVM", "Не вдалось розпарсити час вильоту для ${booking.id}")
            return
        }

        val alarmId = booking.id.hashCode()
        TravelAlarmManager.scheduleTransportReminder(ctx, alarmId, departureMs)

        Log.d("FindVehicleVM", "Сповіщення заплановано для рейсу ${booking.id} на ${departureDateTimeFormat.parse("${booking.date} ${booking.departureTime}")?.time}")
    }

    fun cancelTransportReminder(booking: BookingEntity) {
        val alarmId = booking.id.hashCode()
        TravelAlarmManager.cancel(ctx, alarmId, TravelAlarmManager.ReminderType.TRANSPORT)
        removeAlarm(ctx, alarmId, TravelAlarmManager.ReminderType.TRANSPORT)
    }

    private fun parseDepartureMs(date: String, time: String): Long? {
        if (date.isBlank() || time.isBlank()) return null
        return runCatching {
            departureDateTimeFormat.parse("$date $time")?.time
        }.getOrNull()
    }
}