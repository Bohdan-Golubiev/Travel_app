package com.example.travelapp.viewmodel.profile

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.data.repository.HotelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class MonthSpending(
    val yearMonth: YearMonth,
    val bookingCost: Double,
    val hotelCost: Double,
) {
    val total: Double get() = bookingCost + hotelCost
}

@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.toLocalizedLabel(language: String): String {

    val monthNames = when (language) {
        "uk" -> listOf(
            "Січ", "Лют", "Бер", "Квіт",
            "Трав", "Чер", "Лип", "Серп",
            "Вер", "Жовт", "Лист", "Груд"
        )

        else -> listOf(
            "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec"
        )
    }

    return "${monthNames[monthValue - 1]} ${year.toString().takeLast(2)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalCoroutinesApi::class)
class SpendingStatsViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository = BookingRepository(TravelDB.getInstance(application), application)
    private val hotelRepository   = HotelRepository(TravelDB.getInstance(application), application)

    private val _userId = MutableStateFlow<String?>(null)

    private val _stats = MutableStateFlow<List<MonthSpending>>(emptyList())
    val stats: StateFlow<List<MonthSpending>> = _stats

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    init {
        viewModelScope.launch {
            _userId
                .filterNotNull()
                .flatMapLatest { userId -> buildStatsFlow(userId) }
                .collect { _stats.value = it }
        }
    }

    fun load(userId: String) {
        _userId.value = userId
    }

    private fun buildStatsFlow(userId: String) = combine(
        bookingRepository.getBookings(userId),
        hotelRepository.getHotelsByUser(userId)
    ) { bookings, hotels ->
        val now    = YearMonth.now()
        val months = (5 downTo 0).map { now.minusMonths(it.toLong()) }
        months.map { month ->
            MonthSpending(
                yearMonth   = month,
                bookingCost = bookings.filter { it.belongsToMonth(month) }.sumOf { it.cost },
                hotelCost   = hotels.filter { it.belongsToMonth(month) }.sumOf { it.totalCost },
            )
        }
    }
    private fun BookingEntity.belongsToMonth(month: YearMonth): Boolean = runCatching {
        YearMonth.from(LocalDate.parse(date, formatter)) == month
    }.getOrDefault(false)

    private fun HotelEntity.belongsToMonth(month: YearMonth): Boolean = runCatching {
        YearMonth.from(LocalDate.parse(dateFrom, formatter)) == month
    }.getOrDefault(false)
}
