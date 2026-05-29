package com.example.travelapp.data.repository

import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.entity.UserEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUser(user: UserEntity) {
        db.collection("users")
            .document(user.id)
            .set(user.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun getUser(userId: String): UserEntity? {
        val snap = db.collection("users").
        document(userId).
        get().
        await()
        return if (snap.exists()) snap.toUserEntity() else null
    }

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
        val routeRef = db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)

        val batch = db.batch()

        val placesSnap = routeRef.collection("places").get().await()
        placesSnap.documents.forEach { batch.delete(it.reference) }

        val bookingsSnap = routeRef.collection("bookings").get().await()
        bookingsSnap.documents.forEach { batch.delete(it.reference) }

        val hotelsSnap = routeRef.collection("hotels").get().await()
        hotelsSnap.documents.forEach { batch.delete(it.reference) }

        batch.delete(routeRef)
        batch.commit().await()
    }


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

    suspend fun saveBookings(userId: String, bookings: List<BookingEntity>) {
        if (bookings.isEmpty()) return
        val batch = db.batch()
        bookings.forEach { booking ->
            val ref = db.collection("users")
                .document(userId)
                .collection("routes")
                .document(booking.routeId)
                .collection("bookings")
                .document(booking.id)
            batch.set(ref, booking.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun getBookings(userId: String, routeId: String): List<BookingEntity> {
        val snap = db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("bookings")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toBookingEntity(userId, routeId) }
    }

    suspend fun deleteBooking(userId: String, routeId: String, bookingId: String) {
        db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("bookings")
            .document(bookingId)
            .delete()
            .await()
    }

    // Hotels
    suspend fun saveHotels(userId: String, hotels: List<HotelEntity>) {
        if (hotels.isEmpty()) return
        val batch = db.batch()
        hotels.forEach { hotel ->
            val ref = db.collection("users")
                .document(userId)
                .collection("routes")
                .document(hotel.routeId)
                .collection("hotels")
                .document(hotel.id)
            batch.set(ref, hotel.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun getHotels(userId: String, routeId: String): List<HotelEntity> {
        val snap = db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("hotels")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toHotelEntity(userId, routeId) }
    }

    suspend fun deleteHotel(userId: String, routeId: String, hotelId: String) {
        db.collection("users")
            .document(userId)
            .collection("routes")
            .document(routeId)
            .collection("hotels")
            .document(hotelId)
            .delete()
            .await()
    }

    suspend fun saveReview(review: ReviewEntity) {
        db.collection("reviews")
            .document(review.id)
            .set(review.toMap(), SetOptions.merge())
            .await()

        db.collection("users")
            .document(review.userId)
            .collection("personal_reviews")
            .document(review.id)
            .set(review.toMap(), SetOptions.merge())
            .await()
    }
    suspend fun saveReviews(reviews: List<ReviewEntity>) {
        if (reviews.isEmpty()) return
        val batch = db.batch()

        reviews.forEach { review ->
            val globalRef = db.collection("reviews").document(review.id)
            val userRef = db.collection("users")
                .document(review.userId)
                .collection("personal_reviews")
                .document(review.id)

            batch.set(globalRef, review.toMap(), SetOptions.merge())
            batch.set(userRef, review.toMap(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun deleteReview(reviewId: String, userId: String) {
        db.collection("reviews")
            .document(reviewId)
            .delete()
            .await()

        db.collection("users")
            .document(userId)
            .collection("personal_reviews")
            .document(reviewId)
            .delete()
            .await()
    }

    suspend fun getReviewsByTargetId(targetId: String): List<ReviewEntity> {
        val snap = db.collection("reviews")
            .whereEqualTo("targetId", targetId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get(Source.SERVER)
            .await()
        return snap.documents.mapNotNull { it.toReviewEntity() }
    }

    suspend fun getUserReviews(userId: String): List<ReviewEntity> {
        val snap = db.collection("users")
            .document(userId)
            .collection("personal_reviews")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toReviewEntity() }
    }

    private fun UserEntity.toMap() = mapOf(
        "id"    to id,
        "name"  to name,
        "email" to email
    )

    private fun RouteEntity.toMap() = mapOf(
        "id"          to id,
        "userId"      to userId,
        "name"        to name,
        "createdAt"   to createdAt,
        "description" to description,
        "isFavorite"  to isFavorite,
        "isCompleted" to isCompleted
    )

    private fun PlaceEntity.toMap() = mapOf(
        "id"           to id,
        "googlePlaceId" to googlePlaceId,
        "routeId"      to routeId,
        "name"         to name,
        "location"     to location,
        "orderInRoute" to orderInRoute,
        "visitDate"    to visitDate
    )

    private fun BookingEntity.toMap() = mapOf(
        "id"            to id,
        "userId"        to userId,
        "routeId"       to routeId,
        "type"          to type,
        "name"          to name,
        "departureTime" to departureTime,
        "arrivalTime"   to arrivalTime,
        "date"          to date,
        "from"          to from,
        "to"            to to,
        "createdAt"     to createdAt,
        "cost"          to cost,
        "status"        to status
    )

    private fun HotelEntity.toMap() = mapOf(
        "id"         to id,
        "userId"     to userId,
        "routeId"    to routeId,
        "name"       to name,
        "address"    to address,
        "costPerDay" to costPerDay,
        "dateFrom"   to dateFrom,
        "dateTo"     to dateTo,
        "days"       to days,
        "totalCost"  to totalCost,
        "createdAt"  to createdAt
    )

    private fun ReviewEntity.toMap() = mapOf(
        "userId"     to userId,
        "userName"   to userName,
        "targetId"   to targetId,
        "targetType" to targetType,
        "targetName" to targetName,
        "location"   to location,
        "from"       to from,
        "to"         to to,
        "date"       to date,
        "mark"       to mark,
        "text"       to text,
        "createdAt"  to createdAt
    )

    private fun DocumentSnapshot.toUserEntity() = UserEntity(
        id    = getString("id") ?: id,
        name  = getString("name") ?: "",
        email = getString("email") ?: ""
    )

    private fun DocumentSnapshot.toRouteEntity(userId: String) = RouteEntity(
        id          = getString("id") ?: id,
        userId      = userId,
        name        = getString("name") ?: "",
        createdAt   = getLong("createdAt") ?: 0L,
        description = getString("description") ?: "",
        isFavorite  = getBoolean("isFavorite") ?: false,
        isCompleted = getBoolean("isCompleted") ?: false,
        isSynced    = true
    )

    private fun DocumentSnapshot.toPlaceEntity(routeId: String) = PlaceEntity(
        id            = getString("id") ?: id,
        googlePlaceId = getString("googlePlaceId") ?: "",
        routeId       = routeId,
        name          = getString("name") ?: "",
        location      = getString("location") ?: "",
        orderInRoute  = getLong("orderInRoute")?.toInt() ?: 0,
        visitDate     = getString("visitDate") ?: "",
        isSynced      = true
    )

    private fun DocumentSnapshot.toBookingEntity(
        userId: String,
        routeId: String
    ) = BookingEntity(
        id            = getString("id") ?: id,
        userId        = userId,
        routeId       = routeId,
        type          = getString("type") ?: "",
        name          = getString("name") ?: "",
        departureTime = getString("departureTime") ?: "",
        arrivalTime   = getString("arrivalTime") ?: "",
        date          = getString("date") ?: "",
        from          = getString("from") ?: "",
        to            = getString("to") ?: "",
        createdAt     = getLong("createdAt") ?: 0L,
        cost          = getDouble("cost") ?: 0.0,
        status        = getString("status") ?: "",
        isSynced      = true
    )

    private fun DocumentSnapshot.toHotelEntity(
        userId: String,
        routeId: String
    ) = HotelEntity(
        id         = getString("id") ?: id,
        userId     = userId,
        routeId    = routeId,
        name       = getString("name") ?: "",
        address    = getString("address") ?: "",
        costPerDay = getDouble("costPerDay") ?: 0.0,
        dateFrom   = getString("dateFrom") ?: "",
        dateTo     = getString("dateTo") ?: "",
        days       = getLong("days")?.toInt() ?: 0,
        totalCost  = getDouble("totalCost") ?: 0.0,
        createdAt  = getLong("createdAt") ?: 0L,
        isSynced   = true
    )

    private fun DocumentSnapshot.toReviewEntity(): ReviewEntity? {
        return ReviewEntity(
            id         = getString("id") ?: id,
            userId     = getString("userId") ?: return null,
            userName   = getString("userName") ?: "",
            targetId   = getString("targetId") ?: return null,
            targetType = getString("targetType") ?: return null,
            targetName = getString("targetName") ?: "",
            location   = getString("location") ?: "",
            from       = getString("from") ?: "",
            to         = getString("to") ?: "",
            date       = getString("date") ?: "",
            mark       = getLong("mark")?.toInt() ?: return null,
            text       = getString("text") ?: "",
            createdAt  = getLong("createdAt") ?: System.currentTimeMillis(),
            isSynced   = true
        )
    }
}