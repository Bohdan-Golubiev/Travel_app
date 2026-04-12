package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.data.entity.DeletedRouteEntity

@Dao
interface DeletedRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeletedRouteEntity)

    @Query("SELECT * FROM deleted_routes WHERE userId = :userId")
    suspend fun getAll(userId: String): List<DeletedRouteEntity>

    @Query("DELETE FROM deleted_routes WHERE routeId = :routeId")
    suspend fun delete(routeId: String)
}