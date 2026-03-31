package com.example.travelapp.model.dataclasses

data class Booking(
    val id: Int,
    val name: String,
    val route: String,
    val status: String,
    val createdAt: String,
    val service: String,
    val cost: Int,
    val information: String,
)