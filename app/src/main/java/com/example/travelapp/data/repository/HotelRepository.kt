package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.DeletedHotelEntity
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

    fun getHotelsByUser(userId: String): Flow<List<HotelEntity>> =
        db.hotelDao().getByUser(userId)

    suspend fun deleteHotel(userId: String, hotel: HotelEntity) {
        db.hotelDao().deleteById(hotel.id)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.deleteHotel(userId, hotel.routeId, hotel.id)
            }
        } else {
            db.deletedHotelDao().insert(
                DeletedHotelEntity(
                    hotelId = hotel.id,
                    userId = userId,
                    routeId = hotel.routeId
                )
            )
        }
    }

    suspend fun pushUnsyncedToCloud(userId: String) {
        val unsyncedHotels = db.hotelDao().getUnsynced()
        if (unsyncedHotels.isNotEmpty()) {
            runCatching {
                firestore.saveHotels(userId, unsyncedHotels)
                unsyncedHotels.forEach { db.hotelDao().markSynced(it.id) }
            }
        }

        val pendingDeletions = db.deletedHotelDao().getAll(userId)
        pendingDeletions.forEach { deleted ->
            runCatching {
                firestore.deleteHotel(deleted.userId, deleted.routeId, deleted.hotelId)
                db.deletedHotelDao().delete(deleted.hotelId)
            }
        }
    }
}