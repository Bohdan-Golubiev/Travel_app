package com.example.travelapp.viewmodel.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.FirestoreRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookingDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryReview = ReviewRepository(TravelDB.getInstance(application), application)
    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> = _reviews.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    val avgRating: StateFlow<Double> = reviews
        .map { list ->
            if (list.isEmpty()) 0.0
            else list.sumOf { it.mark }.toDouble() / list.size
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )

    fun loadReviews(targetId: String) {
        viewModelScope.launch {
            _isLoadingReviews.value = true

            val local = repositoryReview.getReviewByTargetId(targetId)

            val remote = runCatching {
                FirestoreRepository().getReviewsByTargetId(targetId)
            }.getOrNull()

            _reviews.value = if (!remote.isNullOrEmpty()) {
                mergeReviews(local, remote)
            } else {
                local
            }

            _isLoadingReviews.value = false
        }
    }
    fun mergeReviews(
        local: List<ReviewEntity>,
        remote: List<ReviewEntity>
    ): List<ReviewEntity> {
        val map = mutableMapOf<String, ReviewEntity>()

        (local + remote).forEach {
            map[it.id] = it
        }

        return map.values.toList()
    }
}