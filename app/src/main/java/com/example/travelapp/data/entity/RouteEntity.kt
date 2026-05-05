package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RouteEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val createdAt: Long,
    val description: String = "",
    val isFavorite: Boolean = false,
    val isSynced: Boolean = false // для локального відстежування (в хмару не йде)
)