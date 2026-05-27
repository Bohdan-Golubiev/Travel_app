package com.example.travelapp.viewmodel.profile

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.dao.BookingWithRoute
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookingRepository(TravelDB.getInstance(application), application)

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
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
            cancelTransportReminder(bookingId)
        }
    }

    fun getByBookingId(bookingId: String): Flow<BookingEntity?> =
        repository.getBookingById(bookingId)

    fun getBookings(userId: String): Flow<List<BookingWithRoute>> =
        repository.getBookingsByUser(userId)

    fun cancelTransportReminder(bookingId: String) {
        val alarmId = bookingId.hashCode()
        TravelAlarmManager.cancel(ctx, alarmId, TravelAlarmManager.ReminderType.TRANSPORT)
        removeAlarm(ctx, alarmId, TravelAlarmManager.ReminderType.TRANSPORT)
        Log.d("DeleteAlarm", "Видалено $bookingId")
    }
}