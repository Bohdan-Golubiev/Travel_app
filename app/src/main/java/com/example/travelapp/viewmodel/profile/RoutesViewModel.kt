package com.example.travelapp.viewmodel.profile

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RoutesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)
    private val db = TravelDB.getInstance(application)
    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val dateFormat     = SimpleDateFormat("dd.MM.yyyy",       Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun getRoutes(userId: String): Flow<List<RouteEntity>> =
        repository.getRoutes(userId)

    fun deleteRoute(userId: String, routeId: String) {
        viewModelScope.launch {
            cancelAlarmsForRoute(routeId)
            repository.deleteRoute(userId, routeId)
        }
    }
    private suspend fun cancelAlarmsForRoute(routeId: String) {
        db.placeDao().getAllByRouteOnce(routeId).forEach { place ->
            val alarmId = place.id.hashCode()
            TravelAlarmManager.cancel(ctx, alarmId, TravelAlarmManager.ReminderType.LOCATION)
            removeAlarm(ctx, alarmId, TravelAlarmManager.ReminderType.LOCATION)
            Log.d("DeleteAlarm", "Видалено ${place.name}")
        }

        db.bookingDao().getAllFromBookings()
            .filter { it.routeId == routeId }
            .forEach { booking ->
                val hasAlarm = parseDateTime(booking.date, booking.departureTime) != null
                if (hasAlarm) {
                    val alarmId = booking.id.hashCode()
                    TravelAlarmManager.cancel(ctx, alarmId, TravelAlarmManager.ReminderType.TRANSPORT)
                    removeAlarm(ctx, alarmId, TravelAlarmManager.ReminderType.TRANSPORT)
                    Log.d("DeleteAlarm", "Видалено ${booking.name}")
                }
            }


        db.hotelDao().getByRoute(routeId).forEach { hotel ->
            val hasAlarm = parseDate(hotel.dateFrom) != null
            if (hasAlarm) {
                val alarmIdIn = (hotel.id+hotel.dateFrom).hashCode()
                TravelAlarmManager.cancel(ctx, alarmIdIn, TravelAlarmManager.ReminderType.CHECK_IN)
                removeAlarm(ctx, alarmIdIn, TravelAlarmManager.ReminderType.CHECK_IN)

                val alarmIdOut = (hotel.id+hotel.dateTo).hashCode()
                TravelAlarmManager.cancel(ctx, alarmIdOut, TravelAlarmManager.ReminderType.CHECK_OUT)
                removeAlarm(ctx, alarmIdOut, TravelAlarmManager.ReminderType.CHECK_OUT)
                Log.d("DeleteAlarm", "Видалено ${hotel.name}")
            }
        }
    }

    fun setRouteCompleted(routeId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setRouteCompleted(routeId, isCompleted)
        }
    }
    private fun parseDate(date: String): Long? {
        if (date.isBlank()) return null
        return runCatching { dateFormat.parse(date)?.time }.getOrNull()
    }
    private fun parseDateTime(date: String, time: String): Long? {
        if (date.isBlank() || time.isBlank()) return null
        return runCatching { dateTimeFormat.parse("$date $time")?.time }.getOrNull()
    }
}