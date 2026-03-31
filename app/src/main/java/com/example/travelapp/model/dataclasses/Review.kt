package com.example.travelapp.model.dataclasses

interface Review {
    val id: Int
    val subject: String
    val text: String
    val assessment: String
    val createdAt: String
}

data class ReviewPlace(
    override val id: Int,
    val place: String,
    override val text: String,
    override val assessment: String,
    override val createdAt: String
) : Review {
    override val subject: String get() = place
}

data class ReviewService(
    override val id: Int,
    val service: String,
    override val text: String,
    override val assessment: String,
    override val createdAt: String
) : Review {
    override val subject: String get() = service
}