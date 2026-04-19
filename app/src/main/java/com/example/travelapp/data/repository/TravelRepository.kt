package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.travelapp.data.entity.DeletedRouteEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.entity.UserEntity
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TravelRepository(
    private val db: TravelDB,
    private val context: Context,
    private val firestore: FirestoreRepository = FirestoreRepository()
) {
    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun saveUser(user: UserEntity) {
        db.userDao().upsert(user)
        runCatching { firestore.saveUser(user) }
    }

// Маршрути
    fun getRoutes(userId: String): Flow<List<RouteEntity>> =
        db.routeDao().getAllByUser(userId)

    suspend fun createRoute(userId: String, name: String, description: String = ""): RouteEntity {
        val route = RouteEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            createdAt = System.currentTimeMillis(),
            description = description,
            isSynced = false
        )
        db.routeDao().upsert(route)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.saveRoute(route)
                db.routeDao().markSynced(route.id)
            }
        }
        return route
    }

    suspend fun deleteRoute(userId: String, routeId: String) {
        db.routeDao().deleteById(routeId)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.deleteRoute(userId, routeId)
            }
        } else {
            db.deletedRouteDao().insert(
                DeletedRouteEntity(routeId = routeId, userId = userId)
            )
        }
    }
    //Місця

    fun getPlaces(routeId: String): Flow<List<PlaceEntity>> =
        db.placeDao().getAllByRoute(routeId)


    suspend fun addPlace(place: PlaceEntity, userId: String) {
        db.placeDao().upsert(place.copy(isSynced = false))
        if (isNetworkAvailable()) {
            runCatching {
                firestore.savePlace(userId, place)
                db.placeDao().markSynced(place.id)
            }
        }
    }

    suspend fun updatePlaceOrder(id: String, newOrder: Int, userId: String, routeId: String) {
        db.placeDao().updateOrder(id, newOrder)
        if(isNetworkAvailable())
        {
            runCatching {
                val places = db.placeDao().getAllByRouteOnce(routeId)
                firestore.savePlaces(userId, places)
                places.forEach { db.placeDao().markSynced(it.id) }
            }
        }
    }
    suspend fun updatePlaceDate(id: String, newOrder: Int, visitDate: String, userId: String, routeId: String) {
        val existing = db.placeDao().getAllByRouteOnce(routeId).find { it.id == id } ?: return
        val updated = existing.copy(orderInRoute = newOrder, visitDate = visitDate, isSynced = false)
        db.placeDao().upsert(updated)
        if (isNetworkAvailable()) {
            runCatching {
                val places = db.placeDao().getAllByRouteOnce(routeId)
                firestore.savePlaces(userId, places)
                places.forEach { db.placeDao().markSynced(it.id) }
            }
        }
    }

    suspend fun deletePlace(userId: String, routeId: String, placeId: String) {
        val places = db.placeDao().getAllByRouteOnce(routeId).toMutableList()
        val idx = places.indexOfFirst { it.id == placeId }
        if (idx == -1) return
        places.removeAt(idx)

        val reordered = places.mapIndexed { i, p -> p.copy(orderInRoute = i, isSynced = false) }
        db.placeDao().deleteAllByRoute(routeId)
        db.placeDao().upsertAll(reordered)

        runCatching {
            firestore.deletePlace(userId, routeId, placeId)
            firestore.savePlaces(userId, reordered)
            reordered.forEach { db.placeDao().markSynced(it.id) }
        }
    }

    suspend fun updateRouteName(userId: String, routeId: String, newName: String) {
        val existing = db.routeDao().getById(routeId) ?: return
        val updated = existing.copy(name = newName, isSynced = false)
        db.routeDao().upsert(updated)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.saveRoute(updated)
                db.routeDao().markSynced(routeId)
            }
        }
    }
    // підтягування з хмари
    //
    suspend fun syncFromCloud(userId: String) {
        val cloudRoutes = firestore.getRoutes(userId)
        db.routeDao().upsertAll(cloudRoutes)

        cloudRoutes.forEach { route ->
            val cloudPlaces = firestore.getPlaces(userId, route.id)
            db.placeDao().upsertAll(cloudPlaces)

            val cloudBookings = firestore.getBookings(userId, route.id)
            db.bookingDao().upsertAll(cloudBookings)

            val cloudHotels = firestore.getHotels(userId, route.id)
            db.hotelDao().upsertAll(cloudHotels)
        }
    }

// відправка несихнонних записів в хмару
    suspend fun pushUnsyncedToCloud(userId: String) {
        val unsyncedRoutes = db.routeDao().getUnsynced(userId)
        unsyncedRoutes.forEach { route ->
            runCatching {
                firestore.saveRoute(route)
                db.routeDao().markSynced(route.id)
            }
        }
        val unsyncedPlaces = db.placeDao().getUnsynced()
        if (unsyncedPlaces.isNotEmpty()) {
            runCatching {
                firestore.savePlaces(userId, unsyncedPlaces)
                unsyncedPlaces.forEach { db.placeDao().markSynced(it.id) }
            }
        }

        val pendingDeletions = db.deletedRouteDao().getAll(userId)
        pendingDeletions.forEach { deleted ->
            runCatching {
                firestore.deleteRoute(deleted.userId, deleted.routeId)
                db.deletedRouteDao().delete(deleted.routeId)
            }
        }
    }
}