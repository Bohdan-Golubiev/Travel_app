package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.dao.BookingWithRoute
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookingRepository(TravelDB.getInstance(application), application)

    fun isBookingExpired(dateStr: String): Boolean {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
            isLenient = false
        }

        val parsedDate = try {
            format.parse(dateStr) ?: return false
        } catch (e: Exception) {
            return false
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return parsedDate.before(today)
    }
    fun deleteBooking(userId: String, routeId: String, bookingId: String) {
        viewModelScope.launch {
            repository.deleteBooking(userId, routeId, bookingId)
        }
    }

    fun getByBookingId(bookingId: String): Flow<BookingEntity?> =
        repository.getBookingById(bookingId)

    fun getBookings(userId: String): Flow<List<BookingWithRoute>> =
        repository.getBookingsByUser(userId)
}