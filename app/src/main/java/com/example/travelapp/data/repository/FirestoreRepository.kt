package com.example.travelapp.data.repository

import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.entity.UserEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // User
    suspend fun saveUser(user: UserEntity) {
        db.collection("users")
            .document(user.id)
            .set(user.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun getUser(userId: String): UserEntity? {
        val snap = db.collection("users").document(userId).get().await()
        return if (snap.exists()) snap.toUserEntity() else null
    }

    // Route
    suspend fun saveRoute(route: RouteEntity) {
        db.collection("users")
            .document(route.userId)
            .collection("routes")
            .document(route.id)
            .set(route.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun getRoutes(userId: String): List<RouteEntity> {
        val snap = db.collection("users")
            .document(userId)
            .collection("routes")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toRouteEntity(userId) }
    }

    suspend fun deleteRoute(userId: String, routeId: String) {
        val placesSnap = db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("places")
            .get()
            .await()
        val batch = db.batch()
        placesSnap.documents.forEach { batch.delete(it.reference) }
        batch.delete(
            db.collection("users")
                .document(userId)
                .collection("routes")
                .document(routeId)
        )
        batch.commit().await()
    }

    // Place

    suspend fun savePlace(userId: String, place: PlaceEntity) {
        db.collection("users")
            .document(userId)
            .collection("routes")
            .document(place.routeId)
            .collection("places")
            .document(place.id)
            .set(place.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun savePlaces(userId: String, places: List<PlaceEntity>) {
        if (places.isEmpty()) return
        val batch = db.batch()
        places.forEach { place ->
            val ref = db.collection("users")
                .document(userId)
                .collection("routes")
                .document(place.routeId)
                .collection("places")
                .document(place.id)
            batch.set(ref, place.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun getPlaces(userId: String, routeId: String): List<PlaceEntity> {
        val snap = db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("places")
            .orderBy("orderInRoute")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toPlaceEntity(routeId) }
    }

    suspend fun deletePlace(userId: String, routeId: String, placeId: String) {
        db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("places")
            .document(placeId)
            .delete()
            .await()
    }

    // конверт

    private fun UserEntity.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "email" to email
    )

    private fun RouteEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "createdAt" to createdAt,
        "description" to description
    )

    private fun PlaceEntity.toMap() = mapOf(
        "id" to id,
        "googlePlaceId" to googlePlaceId,
        "routeId" to routeId,
        "name" to name,
        "location" to location,
        "orderInRoute" to orderInRoute,
        "visitDate" to visitDate
    )

    private fun DocumentSnapshot.toUserEntity() = UserEntity(
        id = getString("id") ?: id,
        name = getString("name") ?: "",
        email = getString("email") ?: ""
    )

    private fun DocumentSnapshot.toRouteEntity(userId: String) = RouteEntity(
        id = getString("id") ?: id,
        userId = userId,
        name = getString("name") ?: "",
        createdAt = getLong("createdAt") ?: 0L,
        description = getString("description") ?: "",
        isSynced = true
    )

    private fun DocumentSnapshot.toPlaceEntity(routeId: String) = PlaceEntity(
        id = getString("id") ?: id,
        googlePlaceId = getString("googlePlaceId") ?: "",
        routeId = routeId,
        name = getString("name") ?: "",
        location = getString("location") ?: "",
        orderInRoute = getLong("orderInRoute")?.toInt() ?: 0,
        visitDate = getString("visitDate") ?: "",
        isSynced = true
    )
}