package com.example.travelapp.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import com.example.travelapp.data.entity.HotelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {

    @Upsert
    suspend fun upsertAll(hotels: List<HotelEntity>)

    @Query("SELECT * FROM hotels WHERE routeId = :routeId")
    suspend fun getByRoute(routeId: String): List<HotelEntity>

    @Query("""
        SELECT h.*, r.name AS routeName
        FROM hotels h
        INNER JOIN routes r ON h.routeId = r.id
        WHERE h.userId = :userId
        ORDER BY h.createdAt DESC
    """)
    fun getHotelWithRoute(userId: String): Flow<List<HotelWithRoute>>

    @Query("""
        SELECT h.*, r.name AS routeName
        FROM hotels h
        INNER JOIN routes r ON h.routeId = r.id
        WHERE h.id = :hotelId
    """)
    fun getByIdWithRoute(hotelId: String): Flow<HotelWithRoute?>

    @Query("DELETE FROM hotels WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM hotels WHERE routeId = :routeId")
    suspend fun deleteByRoute(routeId: String)

    @Query("UPDATE hotels SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM hotels WHERE isSynced = 0")
    suspend fun getUnsynced(): List<HotelEntity>
}

data class HotelWithRoute(
    @Embedded val hotel: HotelEntity,
    @ColumnInfo(name = "routeName") val routeName: String
)