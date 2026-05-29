package com.example.travelapp.viewmodel.profile.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.data.repository.UserRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.model.ReviewTarget
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

class AddReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReviewRepository(TravelDB.getInstance(application), application)
    private val userRepo   = UserRepository(TravelDB.getInstance(application))

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

    fun submitReview(userId: String, target: ReviewTarget) {
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
                    targetId   = target.googlePlaceId,
                    targetType = target.targetType,
                    targetName = target.name,
                    location   = when (target) {
                        is ReviewTarget.Place   -> target.entity.location
                        is ReviewTarget.Hotel   -> target.entity.address
                        is ReviewTarget.Booking -> ""
                    },
                    from = when (target) {
                        is ReviewTarget.Booking -> target.entity.from
                        else                    -> ""
                    },
                    to = when (target) {
                        is ReviewTarget.Booking -> target.entity.to
                        else                    -> ""
                    },
                    date = when (target) {
                        is ReviewTarget.Booking -> target.entity.date
                        is ReviewTarget.Place   -> ""
                        is ReviewTarget.Hotel   -> ""
                    },
                    mark      = state.selectedRating,
                    text      = state.commentText
                        .replace(Regex("\\n\\s*\\n+"), "\n")
                        .trim(),
                    createdAt = System.currentTimeMillis()
                )

                repository.addReview(review)
            }
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}