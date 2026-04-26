package com.example.travelapp.viewmodel.profile.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditReviewUiState(
    val selectedRating: Int = 0,
    val commentText: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = selectedRating > 0 && commentText.isNotBlank() && !isSubmitting
}

class EditReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReviewRepository(TravelDB.getInstance(application), application)

    private val _uiState = MutableStateFlow(EditReviewUiState())
    val uiState: StateFlow<EditReviewUiState> = _uiState.asStateFlow()

    fun initWith(review: ReviewEntity) {
        _uiState.value = _uiState.value.copy(
            selectedRating = review.mark,
            commentText = review.text
        )
    }

    fun onRatingSelected(rating: Int) {
        _uiState.value = _uiState.value.copy(selectedRating = rating)
    }

    fun onCommentChanged(text: String) {
        if (text.length <= 1000) {
            _uiState.value = _uiState.value.copy(commentText = text)
        }
    }

    fun submitEdit(original: ReviewEntity) {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            runCatching {
                val updated = original.copy(
                    mark = state.selectedRating,
                    text = state.commentText
                        .replace(Regex("\\n\\s*\\n+"), "\n")
                        .trim(),
                    isSynced = false
                )
                repository.addReview(updated)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isSubmitted = true
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Помилка збереження"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}