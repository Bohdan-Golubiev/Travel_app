package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.data.repository.UserRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddReviewUiState(
    val selectedRating: Int = 0,
    val commentText: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = selectedRating > 0 && commentText.isNotBlank() && !isSubmitting
}

class AddReviewPlaceViewModel( application: Application) : AndroidViewModel(application) {
    private val repository = ReviewRepository(TravelDB.getInstance(application), application)
    private val userRepo = UserRepository(TravelDB.getInstance(application))

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState: StateFlow<AddReviewUiState> = _uiState.asStateFlow()

    fun onRatingSelected(rating: Int) {
        _uiState.update { it.copy(selectedRating = rating) }
    }

    fun onCommentChanged(text: String) {
        if (text.length <= 1000) {
            _uiState.update { it.copy(commentText = text) }
        }
    }

    fun submitReview(userId: String, place: PlaceEntity) {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            runCatching {
                val user = userRepo.getUserName(userId)

                val review = ReviewEntity(
                    id         = UUID.randomUUID().toString(),
                    userId     = userId,
                    userName   = user?.name ?: "Unknown",
                    targetId   = place.googlePlaceId,
                    targetType = "place",
                    mark       = state.selectedRating,
                    text       = state.commentText.trim(),
                    createdAt  = System.currentTimeMillis()
                )

                repository.addReview(review)
            }
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}