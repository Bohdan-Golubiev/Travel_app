package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.data.entity.DeletedBookingEntity

@Dao
interface DeletedBookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeletedBookingEntity)

    @Query("SELECT * FROM deleted_bookings WHERE userId = :userId")
    suspend fun getAll(userId: String): List<DeletedBookingEntity>

    @Query("DELETE FROM deleted_bookings WHERE bookingId = :bookingId")
    suspend fun delete(bookingId: String)
}