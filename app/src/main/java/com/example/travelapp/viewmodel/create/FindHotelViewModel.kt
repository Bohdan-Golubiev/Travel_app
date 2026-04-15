package com.example.travelapp.viewmodel.create

import androidx.lifecycle.ViewModel
import com.example.travelapp.view.create.HotelOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HotelItemState(
    val dateFrom: String = "",
    val dateTo: String = "",
    val isExpanded: Boolean = false
) {
    val days: Int get() {
        val from = parseDateMillis(dateFrom)
        val to = parseDateMillis(dateTo)
        return if (from != null && to != null) daysBetween(from, to) else 0
    }
}

data class FindHotelUiState(
    val startPlace: String = "",
    val itemStates: List<HotelItemState> = hotelOptions.map { HotelItemState() }
)

val hotelOptions = listOf(
    HotelOption("Hotel room option", "Cost per day -", costPerDayValue = 100),
    HotelOption("Hotel room option", "Cost per day -", costPerDayValue = 150),
    HotelOption("Hotel room option", "Cost per day -", costPerDayValue = 200),
    HotelOption("Hotel room option", "Cost per day -", costPerDayValue = 250),
)

class FindHotelViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FindHotelUiState())
    val uiState: StateFlow<FindHotelUiState> = _uiState.asStateFlow()

    fun onStartPlaceChange(value: String) {
        _uiState.update { it.copy(startPlace = value) }
    }

    fun toggleExpand(index: Int) {
        _uiState.update { state ->
            state.copy(itemStates = state.itemStates.mapIndexed { i, item ->
                if (i == index) item.copy(isExpanded = !item.isExpanded) else item
            })
        }
    }

    fun onDateFromSelected(index: Int, millis: Long) {
        _uiState.update { state ->
            state.copy(itemStates = state.itemStates.mapIndexed { i, item ->
                if (i == index) item.copy(dateFrom = formatDate(millis)) else item
            })
        }
    }

    fun onDateToSelected(index: Int, millis: Long) {
        _uiState.update { state ->
            state.copy(itemStates = state.itemStates.mapIndexed { i, item ->
                if (i == index) item.copy(dateTo = formatDate(millis)) else item
            })
        }
    }

    fun onAddClick(index: Int) {
    }
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))

fun parseDateMillis(date: String): Long? = try {
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date)?.time
} catch (e: Exception) { null }

fun daysBetween(fromMillis: Long, toMillis: Long): Int {
    val diff = toMillis - fromMillis
    return if (diff > 0) (diff / (1000 * 60 * 60 * 24)).toInt() else 0
}