package com.example.travelapp.data.dao

import androidx.room.*
import com.example.travelapp.data.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Upsert
    suspend fun upsert(place: PlaceEntity)

    @Upsert
    suspend fun upsertAll(places: List<PlaceEntity>)

    @Query("SELECT * FROM places WHERE routeId = :routeId ORDER BY orderInRoute ASC")
    fun getAllByRoute(routeId: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE routeId = :routeId ORDER BY orderInRoute ASC")
    suspend fun getAllByRouteOnce(routeId: String): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE isSynced = 0")
    suspend fun getUnsynced(): List<PlaceEntity>

    @Query("UPDATE places SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Delete
    suspend fun delete(place: PlaceEntity)

    @Query("DELETE FROM places WHERE routeId = :routeId")
    suspend fun deleteAllByRoute(routeId: String)

    @Query("UPDATE places SET orderInRoute = :order, isSynced = 0 WHERE id = :id")
    suspend fun updateOrder(id: String, order: Int)

    @Query("""
        SELECT DISTINCT routeId FROM places
        WHERE visitDate != ''
        GROUP BY routeId
        HAVING
            MIN(
                substr(visitDate, 7, 4) || '-' ||
                substr(visitDate, 4, 2) || '-' ||
                substr(visitDate, 1, 2)
            ) <= :today
            AND
            MAX(
                substr(visitDate, 7, 4) || '-' ||
                substr(visitDate, 4, 2) || '-' ||
                substr(visitDate, 1, 2)
            ) >= :today
    """)
    fun getActiveRouteIds(today: String): Flow<List<String>>

    @Query("""
    SELECT * FROM places
    WHERE routeId = :routeId
      AND visitDate != ''
      AND (
        substr(visitDate, 7, 4) || '-' ||
        substr(visitDate, 4, 2) || '-' ||
        substr(visitDate, 1, 2)
      ) >= :today
    ORDER BY
        substr(visitDate, 7, 4) || '-' ||
        substr(visitDate, 4, 2) || '-' ||
        substr(visitDate, 1, 2)
    ASC
    LIMIT 1
""")
    fun getNextPlace(routeId: String, today: String): Flow<PlaceEntity?>
}