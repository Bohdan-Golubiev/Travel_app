package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.travelapp.data.dao.BookingWithRoute
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.DeletedBookingEntity
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

    fun getBookings(userId: String): Flow<List<BookingEntity>> =
        db.bookingDao().getAllByUser(userId)

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
        } else {
            db.deletedBookingDao().insert(
                DeletedBookingEntity(
                    bookingId = bookingId,
                    userId = userId,
                    routeId = routeId
                )
            )
        }
    }

    //поки хай буде
    suspend fun syncFromCloud(userId: String, routeIds: List<String>) {
        routeIds.forEach { routeId ->
            val cloudBookings = firestore.getBookings(userId, routeId)
            db.bookingDao().upsertAll(cloudBookings)
        }
    }
    suspend fun pushUnsyncedToCloud(userId: String) {
        val unsyncedBookings = db.bookingDao().getUnsynced()
        if (unsyncedBookings.isNotEmpty()) {
            runCatching {
                firestore.saveBookings(userId, unsyncedBookings)
                unsyncedBookings.forEach { db.bookingDao().markSynced(it.id) }
            }
        }

        val pendingDeletions = db.deletedBookingDao().getAll(userId)
        pendingDeletions.forEach { deleted ->
            runCatching {
                firestore.deleteBooking(deleted.userId, deleted.routeId, deleted.bookingId)
                db.deletedBookingDao().delete(deleted.bookingId)
            }
        }
    }
}