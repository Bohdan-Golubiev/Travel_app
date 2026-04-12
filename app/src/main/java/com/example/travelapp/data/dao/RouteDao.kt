package com.example.travelapp.data.dao

import androidx.room.*
import com.example.travelapp.data.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Upsert
    suspend fun upsert(route: RouteEntity)

    @Upsert
    suspend fun upsertAll(routes: List<RouteEntity>)

    @Query("SELECT * FROM routes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllByUser(userId: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getById(id: String): RouteEntity?

    // Маршрути, які ще не синхронізовані з хмарою
    @Query("SELECT * FROM routes WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<RouteEntity>

    @Query("UPDATE routes SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Delete
    suspend fun delete(route: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteById(id: String)
}