package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("userId"),
    ]
)
data class ReviewEntity(
    @PrimaryKey
    val id          : String,
    val userId      : String,
    val userName    : String = "",
    val targetId    : String,
    val targetType  : String,
    val targetName  : String,
    val location    : String,
    val from        : String,
    val to          : String,
    val date        : String,
    val mark        : Int,
    val text        : String,
    val createdAt   : Long    = System.currentTimeMillis(),
    val isSynced    : Boolean = false
)