package com.example.travelapp.viewmodel.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.FirestoreRepository
import com.example.travelapp.data.repository.HotelRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HotelRepository(
        db = TravelDB.getInstance(application),
        context = application
    )
    private val repositoryReview = ReviewRepository(TravelDB.getInstance(application), application)
    private val dao = TravelDB.getInstance(application).hotelDao()

    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> = _reviews.asStateFlow()
    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    fun getHotelsByUser(userId: String): Flow<List<HotelWithRoute>> =
        dao.getHotelWithRoute(userId)

    fun getHotelWithRouteById(hotelId: String): Flow<HotelWithRoute?> =
        dao.getByIdWithRoute(hotelId)

    fun deleteHotel(userId: String, hotel: HotelEntity) {
        viewModelScope.launch {
            repository.deleteHotel(userId, hotel)
        }
    }

    fun loadReviews(targetId: String) {
        viewModelScope.launch {
            _isLoadingReviews.value = true
            _reviews.value = repositoryReview.getReviewByTargetId(targetId)

            runCatching {
                val remoteReviews = FirestoreRepository().getReviewsByTargetId(targetId)
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