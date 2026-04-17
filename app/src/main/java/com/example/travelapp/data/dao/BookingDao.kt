package com.example.travelapp.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import com.example.travelapp.data.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE routeId = :routeId")
    fun getAllByRoute(routeId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE userId = :userId")
    fun getAllByUser(userId: String): Flow<List<BookingEntity>>

    @Upsert
    suspend fun upsertAll(bookings: List<BookingEntity>)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE bookings SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM bookings WHERE isSynced = 0")
    suspend fun getUnsynced(): List<BookingEntity>

    @Query("""
    SELECT b.*, r.name as routeName
    FROM bookings b
    INNER JOIN routes r ON b.routeId = r.id
    WHERE b.userId = :userId
""")
    fun getBookingsWithRoute(userId: String): Flow<List<BookingWithRoute>>
}

data class BookingWithRoute(
    @Embedded val booking: BookingEntity,
    @ColumnInfo(name = "routeName") val routeName: String
)