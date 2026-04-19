package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.travelapp.data.dao.BookingWithRoute
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow

class HotelRepository(
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

    suspend fun saveHotels(userId: String, hotels: List<HotelEntity>) {
        db.hotelDao().upsertAll(hotels)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.saveHotels(userId, hotels)
                hotels.forEach { db.hotelDao().markSynced(it.id) }
            }
        }
    }

    fun getHotelByRoute(routeId: String): Flow<List<HotelWithRoute>> =
        db.hotelDao().getHotelWithRoute(routeId)

    suspend fun deleteHotel(userId: String, hotel: HotelEntity) {
        db.hotelDao().deleteById(hotel.id)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.deleteHotel(userId, hotel.routeId, hotel.id)
            }
        }
    }

    suspend fun pushUnsyncedToCloud(userId: String) {
        val unsyncedHotels = db.hotelDao().getUnsynced()
        if (unsyncedHotels.isNotEmpty()) {
            runCatching {
                firestore.saveHotels(userId, unsyncedHotels)
                unsyncedHotels.forEach { db.bookingDao().markSynced(it.id) }
            }
        }
    }
}