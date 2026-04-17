package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_bookings")
data class DeletedBookingEntity(
    @PrimaryKey
    val bookingId: String,
    val userId: String,
    val routeId: String
)