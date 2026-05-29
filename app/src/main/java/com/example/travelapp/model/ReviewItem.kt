package com.example.travelapp.model

import com.example.travelapp.data.entity.ReviewEntity

sealed class ReviewItem {
    abstract val review: ReviewEntity

    data class PlaceReview(
        override val review: ReviewEntity
    ) : ReviewItem()

    data class HotelReview(
        override val review: ReviewEntity
    ) : ReviewItem()

    data class BookingReview(
        override val review: ReviewEntity
    ) : ReviewItem()
}