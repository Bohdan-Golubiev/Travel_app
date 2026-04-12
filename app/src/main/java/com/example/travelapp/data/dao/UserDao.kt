package com.example.travelapp.data.dao

import androidx.room.*
import com.example.travelapp.data.entity.UserEntity

@Dao
interface UserDao {

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Delete
    suspend fun delete(user: UserEntity)
}