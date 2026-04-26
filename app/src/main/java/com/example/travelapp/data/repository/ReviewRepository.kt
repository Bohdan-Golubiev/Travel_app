package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.travelapp.data.entity.DeletedReviewEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.db.TravelDB
class ReviewRepository(
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

    suspend fun addReview(review: ReviewEntity) {
        db.reviewDao().upsert(review)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.saveReview(review)
                db.reviewDao().markSynced(review.id)
            }
        }
    }

    suspend fun getReviewsByUserId(userId: String): List<ReviewEntity> =
        db.reviewDao().getByUserId(userId)

    suspend fun getReviewByTargetId(targetId: String): List<ReviewEntity> =
        db.reviewDao().getByTargetId(targetId)

    suspend fun getAllReviews(): List<ReviewEntity> =
        db.reviewDao().getAll()

    suspend fun getPlaceById(targetId: String): PlaceEntity? =
        db.reviewDao().getByPlaceId(targetId)

    suspend fun getHotelByKey(hotelKey: String): HotelEntity? =
        db.hotelDao().getAll().firstOrNull { it.id.startsWith(hotelKey) } //може бути бобо

    suspend fun deleteReview(userId: String, review: ReviewEntity) {
        db.reviewDao().deleteById(review.id)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.deleteReview(review.id, userId)
            }
        }else{
            db.deletedReviewDao().insert(
                DeletedReviewEntity(
                    reviewId =  review.id,
                    userId = userId
                )
            )
        }
    }

    suspend fun pushUnsyncedToCloud(userId: String) {
        val pendingDeletions = db.deletedReviewDao().getAll(userId)
        val deletedIds = pendingDeletions.map { it.reviewId }.toSet()

        val unsyncedReviews = db.reviewDao()
            .getUnsynced()
            .filter { it.id !in deletedIds }

        if (unsyncedReviews.isNotEmpty()) {
            runCatching {
                firestore.saveReviews(unsyncedReviews)
            }.onSuccess {
                unsyncedReviews.forEach { db.reviewDao().markSynced(it.id) }
            }
        }

        pendingDeletions.forEach { deleted ->
            runCatching {
                firestore.deleteReview(deleted.reviewId, userId)
            }.onSuccess {
                db.deletedReviewDao().delete(deleted.reviewId)
            }
        }
    }
}