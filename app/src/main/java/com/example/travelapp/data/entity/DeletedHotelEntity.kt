package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_hotel")
data class DeletedHotelEntity(
    @PrimaryKey
    val hotelId: String,
    val userId: String,
    val routeId: String
)