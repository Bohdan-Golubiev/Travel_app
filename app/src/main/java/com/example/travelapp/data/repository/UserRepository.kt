package com.example.travelapp.data.repository

import com.example.travelapp.data.entity.UserEntity
import com.example.travelapp.db.TravelDB

class UserRepository(
    private val db: TravelDB,
    private val firestore: FirestoreRepository = FirestoreRepository()
){
    suspend fun getUserName(userId: String): UserEntity? {
        return db.userDao().getById(userId)
    }

    suspend fun saveUser(user: UserEntity) {
        db.userDao().upsert(user)
        runCatching { firestore.saveUser(user) }
    }
}