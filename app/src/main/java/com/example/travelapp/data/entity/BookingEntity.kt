package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookings",
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
data class BookingEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val routeId: String,
    val type: String,           // "Pl" / "Tr" / "Bs"
    val name: String,           // "LOT - Polish Airlines · LO5452"
    val departureTime: String,  // "13:05"
    val arrivalTime: String,    // "14:25"
    val date: String,           // "2026-04-14"
    val from: String,           // "CPH"
    val to: String,             // "WAW"
    val createdAt: Long,        // System.currentTimeMillis()
    val cost: Double = 0.0,
    val status: String,
    val isSynced: Boolean = false
)