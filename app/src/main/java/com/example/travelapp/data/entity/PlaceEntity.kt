package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "places",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routeId")]
)
data class PlaceEntity(
    @PrimaryKey
    val id: String,             // UUID унікальний запис в таблиці
    val googlePlaceId: String,  // для запитів до Places API
    val routeId: String,
    val name: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val orderInRoute: Int,
    val visitDate: String = "", // "dd.MM.yyyy"
    val isSynced: Boolean = false // для локального відстежування (в хмару не йде)
)