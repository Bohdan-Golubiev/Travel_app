package com.example.travelapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    suspend fun deleteReview(userId: String, review: ReviewEntity) {
        db.reviewDao().deleteById(review.id)
        if (isNetworkAvailable()) {
            runCatching {
                firestore.deleteReview(review.id, userId)
            }
        }
    }

    suspend fun pushUnsyncedToCloud(){
        val unsyncedReviews = db.reviewDao().getUnsynced()
        if (unsyncedReviews.isNotEmpty()) {
            runCatching {
                firestore.saveReviews(unsyncedReviews)
                unsyncedReviews.forEach { db.bookingDao().markSynced(it.id) }
            }
        }
        //видалені відгуки
    }
}