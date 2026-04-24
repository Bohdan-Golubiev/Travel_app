package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.data.entity.DeletedReviewEntity

@Dao
interface DeletedReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeletedReviewEntity)

    @Query("SELECT * FROM deleted_review WHERE userId = :userId")
    suspend fun getAll(userId: String): List<DeletedReviewEntity>

    @Query("DELETE FROM deleted_review WHERE reviewId = :reviewId")
    suspend fun delete(reviewId: String)
}