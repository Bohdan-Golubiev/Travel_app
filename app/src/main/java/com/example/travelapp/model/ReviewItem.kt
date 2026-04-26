package com.example.travelapp.model

import com.example.travelapp.data.entity.ReviewEntity

sealed class ReviewItem {
    abstract val review: ReviewEntity

    data class PlaceReview(
        override val review: ReviewEntity,
        val placeName: String,
        val placeLocation: String
    ) : ReviewItem()

    data class HotelReview(
        override val review: ReviewEntity,
        val hotelName: String,
        val hotelAddress: String
    ) : ReviewItem()
}