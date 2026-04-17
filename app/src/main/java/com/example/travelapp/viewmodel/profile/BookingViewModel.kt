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

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookingRepository(TravelDB.getInstance(application), application)

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