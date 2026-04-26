package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity

@Dao
interface ReviewDao {

    @Upsert
    suspend fun upsertAll(reviews: List<ReviewEntity>)
    @Upsert
    suspend fun upsert(review: ReviewEntity)

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReviewEntity>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUserId(userId: String): List<ReviewEntity>

    @Query("SELECT * FROM places WHERE googlePlaceId = :placeId LIMIT 1")
    suspend fun getByPlaceId(placeId: String): PlaceEntity?

    @Query("SELECT * FROM reviews WHERE targetId = :targetId ORDER BY createdAt DESC")
    suspend fun getByTargetId(targetId: String): List<ReviewEntity>

    @Query("SELECT * FROM reviews WHERE isSynced = 0")
    suspend fun getUnsynced(): List<ReviewEntity>

    @Query("UPDATE reviews SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteById(id: String)
}