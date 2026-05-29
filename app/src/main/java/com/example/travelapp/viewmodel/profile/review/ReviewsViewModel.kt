package com.example.travelapp.viewmodel.profile.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.model.ReviewItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val placeReviews: List<ReviewItem.PlaceReview> = emptyList(),
    val hotelReviews: List<ReviewItem.HotelReview> = emptyList(),
    val bookingReviews: List<ReviewItem.BookingReview> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReviewsViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository = ReviewRepository(TravelDB.getInstance(application), application)

    private val _uiState = MutableStateFlow(ReviewsUiState(isLoading = true))
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    fun loadReviews(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching {
                val reviews = repository.getReviewsByUserId(userId)

                val placeReviews   = mutableListOf<ReviewItem.PlaceReview>()
                val hotelReviews   = mutableListOf<ReviewItem.HotelReview>()
                val bookingReviews = mutableListOf<ReviewItem.BookingReview>()

                reviews.forEach { review ->
                    when (review.targetType) {
                        "place"   -> placeReviews   += ReviewItem.PlaceReview(review)
                        "hotel"   -> hotelReviews   += ReviewItem.HotelReview(review)
                        "booking" -> bookingReviews += ReviewItem.BookingReview(review)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    placeReviews   = placeReviews,
                    hotelReviews   = hotelReviews,
                    bookingReviews = bookingReviews,
                    isLoading      = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteReview(userId: String, review: ReviewEntity) {
        viewModelScope.launch {
            repository.deleteReview(userId, review)
        }
        _uiState.value = _uiState.value.copy(
            placeReviews   = _uiState.value.placeReviews.filter   { it.review.id != review.id },
            hotelReviews   = _uiState.value.hotelReviews.filter   { it.review.id != review.id },
            bookingReviews = _uiState.value.bookingReviews.filter { it.review.id != review.id }
        )
    }
}