package com.example.travelapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.data.repository.HotelRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val repository = TravelRepository(TravelDB.getInstance(applicationContext), applicationContext)
        val repositoryBooking = BookingRepository(TravelDB.getInstance(applicationContext), applicationContext)
        val repositoryHotels = HotelRepository(TravelDB.getInstance(applicationContext), applicationContext)
        val repositoryReviews = ReviewRepository(TravelDB.getInstance(applicationContext), applicationContext)
        return try {
            repository.pushUnsyncedToCloud(userId)
            repositoryBooking.pushUnsyncedToCloud(userId)
            repositoryHotels.pushUnsyncedToCloud(userId)
            repositoryReviews.pushUnsyncedToCloud()

            repository.syncFromCloud(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}