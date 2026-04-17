package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.travelapp.data.dao.BookingWithRoute
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow

class BookingRepository(
    private val db: TravelDB,
    private val context: Context,
    private val firestore: FirestoreRepository = FirestoreRepository()
) {
    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getBookingsByUser(userId: String): Flow<List<BookingWithRoute>> =
        db.bookingDao().getBookingsWithRoute(userId)

    fun getBookings(routeId: String): Flow<List<BookingEntity>> =
        db.bookingDao().getAllByRoute(routeId)

    fun getBookingById(bookingId: String): Flow<BookingEntity?> =
        db.bookingDao().getBookingById(bookingId)

    suspend fun saveBookings(bookings: List<BookingEntity>, userId: String) {
        db.bookingDao().upsertAll(bookings)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.saveBookings(userId, bookings)
                bookings.forEach { db.bookingDao().markSynced(it.id) }
            }
        }
    }

    suspend fun deleteBooking(userId: String, routeId: String, bookingId: String) {
        db.bookingDao().deleteById(bookingId)
        if (isNetworkAvailable()) {
            runCatching { firestore.deleteBooking(userId, routeId, bookingId) }
        }
    }
}