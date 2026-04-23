package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.view.profile.ReviewWithPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val reviews: List<ReviewWithPlace> = emptyList(),
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

            try {
                val reviews = repository.getReviewsByUserId(userId)

                val reviewsWithPlaces = reviews.map { review ->
                    val place = repository.getPlaceById(review.targetId)

                    ReviewWithPlace(
                        review = review,
                        placeName = place?.name,
                        placeLocation = place?.location
                    )
                }

                _uiState.value = _uiState.value.copy(
                    reviews = reviewsWithPlaces,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteReview(userId: String,review: ReviewEntity) {
        viewModelScope.launch {
            repository.deleteReview(userId,review)
        }
        _uiState.value = _uiState.value.copy(
            reviews = _uiState.value.reviews.filter { it.review.id != review.id }
        )
    }
}