package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_review")
data class DeletedReviewEntity (
    @PrimaryKey
    val reviewId: String,
    val userId: String
)