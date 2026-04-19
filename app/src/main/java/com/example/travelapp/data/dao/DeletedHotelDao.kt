package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.data.entity.DeletedHotelEntity

@Dao
interface DeletedHotelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeletedHotelEntity)

    @Query("SELECT * FROM deleted_hotel WHERE userId = :userId")
    suspend fun getAll(userId: String): List<DeletedHotelEntity>

    @Query("DELETE FROM deleted_hotel WHERE hotelId = :hotelId")
    suspend fun delete(hotelId: String)
}