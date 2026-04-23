package com.example.travelapp.viewmodel.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.FirestoreRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)
    private val repositoryReview = ReviewRepository(TravelDB.getInstance(application), application)

    private val _place = MutableStateFlow<PlaceEntity?>(null)
    val place: StateFlow<PlaceEntity?> = _place.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> = _reviews.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    fun loadPlace(placeId: String, routeId: String) {
        viewModelScope.launch {
            repository.getPlaces(routeId)
                .collect { places ->
                    val found = places.find { it.id == placeId }
                    _place.value = found
                    found?.googlePlaceId?.takeIf { it.isNotEmpty() }?.let {
                        loadReviews(it)
                    }
                }
        }
    }

    private fun loadReviews(googlePlaceId: String) {
        viewModelScope.launch {
            _isLoadingReviews.value = true
            _reviews.value = repositoryReview.getReviewByTargetId(googlePlaceId)

            runCatching {
                val remoteReviews = FirestoreRepository().getReviewsByTargetId(googlePlaceId)
                if (remoteReviews.isNotEmpty()) {
                    _reviews.value = remoteReviews
                }
            }.onFailure {
                Log.e("FIRESTORE", "Error loading reviews", it)
            }
            _isLoadingReviews.value = false
        }
    }
}