package com.example.travelapp.viewmodel.create.routes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.FirestoreRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.data.repository.WeatherInfo
import com.example.travelapp.data.repository.WeatherRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)
    private val repositoryReview = ReviewRepository(TravelDB.getInstance(application), application)
    private val weatherRepository = WeatherRepository()
    private val _place = MutableStateFlow<PlaceEntity?>(null)
    val place: StateFlow<PlaceEntity?> = _place.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> = _reviews.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    private val _isLoadingWeather = MutableStateFlow(false)
    val isLoadingWeather: StateFlow<Boolean> = _isLoadingWeather.asStateFlow()

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
    fun loadPlace(placeId: String, routeId: String) {
        viewModelScope.launch {
            repository.getPlaces(routeId)
                .collect { places ->
                    val found = places.find { it.id == placeId }
                    _place.value = found

                    found?.let {
                        it.googlePlaceId.takeIf { id -> id.isNotEmpty() }?.let { id ->
                            loadReviews(id)
                        }
                        if (it.visitDate.isNotBlank()) {
                            loadWeather(it)
                        }
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
    private fun loadWeather(place: PlaceEntity) {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            _weather.value = null

            val query = if (place.location.isNotBlank()) {
                "${place.name}, ${place.location}"
            } else {
                place.name
            }

            _weather.value = weatherRepository.getForecast(query, place.visitDate)
            _isLoadingWeather.value = false
        }
    }
}