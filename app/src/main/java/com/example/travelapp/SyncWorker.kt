package com.example.travelapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.travelapp.data.repository.AirportRepository
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
        val db = TravelDB.getInstance(applicationContext)

        val repository = TravelRepository(db, applicationContext)
        val repositoryBooking = BookingRepository(db, applicationContext)
        val repositoryHotels = HotelRepository(db, applicationContext)
        val repositoryReviews = ReviewRepository(db, applicationContext)
        val repositoryAirports = AirportRepository(applicationContext, db)

        try {
            repositoryAirports.syncAirportsFromAssetsIfNeeded()
        } catch (e: Exception) {
        }

        return try {
            repository.pushUnsyncedToCloud(userId)
            repositoryBooking.pushUnsyncedToCloud(userId)
            repositoryHotels.pushUnsyncedToCloud(userId)
            repositoryReviews.pushUnsyncedToCloud(userId)

            repository.syncFromCloud(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}