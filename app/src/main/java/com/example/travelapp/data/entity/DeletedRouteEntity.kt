package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_routes")
data class DeletedRouteEntity(
    @PrimaryKey
    val routeId: String,
    val userId: String,
    val deletedAt: Long = System.currentTimeMillis()
)