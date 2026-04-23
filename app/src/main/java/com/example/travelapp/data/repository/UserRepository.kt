package com.example.travelapp.data.repository

import com.example.travelapp.data.entity.UserEntity
import com.example.travelapp.db.TravelDB

class UserRepository(
    private val db: TravelDB,
){
    suspend fun getUserName(userId: String): UserEntity? {
        return db.userDao().getById(userId)
    }
}