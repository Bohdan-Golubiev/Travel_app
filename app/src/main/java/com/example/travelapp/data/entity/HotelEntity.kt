package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hotels",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("routeId")
    ]
)
data class HotelEntity(
    @PrimaryKey
    val id          : String,
    val userId      : String,
    val routeId     : String,
    val name        : String,
    val address     : String,
    val costPerDay  : Double,
    val dateFrom    : String,
    val dateTo      : String,
    val days        : Int,
    val totalCost   : Double,
    val createdAt   : Long    = System.currentTimeMillis(),
    val isSynced    : Boolean = false
)