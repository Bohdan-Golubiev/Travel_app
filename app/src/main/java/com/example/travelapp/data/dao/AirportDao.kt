package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.data.entity.AirportEntity

@Dao
interface AirportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(airports: List<AirportEntity>)

    @Query("SELECT * FROM airport")
    suspend fun getAll(): List<AirportEntity>

    @Query("SELECT COUNT(*) FROM airport")
    suspend fun count(): Int

    @Query("""
        SELECT * FROM airport 
        WHERE municipality LIKE '%' || :query || '%' 
        COLLATE NOCASE 
        LIMIT 5
    """)
    suspend fun searchByCity(query: String): List<AirportEntity>
}