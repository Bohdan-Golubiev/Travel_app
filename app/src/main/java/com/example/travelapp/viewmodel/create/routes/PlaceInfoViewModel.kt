package com.example.travelapp.viewmodel.create.routes

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.FirestoreRepository
import com.example.travelapp.data.repository.ReviewRepository
import com.example.travelapp.data.repository.WeatherInfo
import com.example.travelapp.data.repository.WeatherRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.viewmodel.create.PlaceItem
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlaceInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewRepository = ReviewRepository(TravelDB.getInstance(application), application)
    private val weatherRepository = WeatherRepository()

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    private val _isLoadingWeather = MutableStateFlow(false)
    val isLoadingWeather: StateFlow<Boolean> = _isLoadingWeather.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> = _reviews.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()
    private val _photos = MutableStateFlow<List<Bitmap>>(emptyList())
    val photos: StateFlow<List<Bitmap>> = _photos.asStateFlow()
    private val _isLoadingPhotos = MutableStateFlow(false)
    val isLoadingPhotos: StateFlow<Boolean> = _isLoadingPhotos.asStateFlow()

    val avgRating: StateFlow<Double> = _reviews
        .map { list ->
            if (list.isEmpty()) 0.0
            else list.sumOf { it.mark }.toDouble() / list.size
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun load(place: PlaceItem) {
        loadReviews(place.googlePlaceId)
        loadPhotos(place.googlePlaceId)
        if (place.visitDate.isNotBlank()) {
            loadWeather(place)
        }
    }


    private fun loadReviews(googlePlaceId: String) {
        if (googlePlaceId.isBlank()) return
        viewModelScope.launch {
            _isLoadingReviews.value = true
            _reviews.value = reviewRepository.getReviewByTargetId(googlePlaceId)

            runCatching {
                val remote = FirestoreRepository().getReviewsByTargetId(googlePlaceId)
                if (remote.isNotEmpty()) {
                    _reviews.value = remote
                }
            }.onFailure {
                Log.e("PlaceInfoVM", "Firestore reviews error", it)
            }

            _isLoadingReviews.value = false
        }
    }


    private fun loadWeather(place: PlaceItem) {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            _weather.value = null

            val query = if (place.location.isNotBlank()) {
                "${place.name}, ${place.location}"
            } else {
                place.name
            }

            runCatching {
                _weather.value = weatherRepository.getForecast(query, place.visitDate)
            }.onFailure {
                Log.e("PlaceInfoVM", "Weather error", it)
            }

            _isLoadingWeather.value = false
        }
    }
    private fun loadPhotos(googlePlaceId: String) {
        viewModelScope.launch {
            _isLoadingPhotos.value = true
            _photos.value = emptyList()

            runCatching {
                val placesClient = Places.createClient(getApplication())

                val metaRequest = FetchPlaceRequest.newInstance(
                    googlePlaceId,
                    listOf(Place.Field.PHOTO_METADATAS)
                )

                placesClient.fetchPlace(metaRequest)
                    .addOnSuccessListener { response ->
                        val metadatas = response.place.photoMetadatas
                            ?.take(7)
                            ?: run {
                                _isLoadingPhotos.value = false
                                return@addOnSuccessListener
                            }

                        viewModelScope.launch {
                            val bitmaps = metadatas.mapNotNull { meta ->
                                runCatching {
                                    val photoRequest = FetchPhotoRequest.builder(meta)
                                        .setMaxWidth(1080)
                                        .setMaxHeight(810)
                                        .build()
                                    suspendCancellableCoroutine { cont ->
                                        placesClient.fetchPhoto(photoRequest)
                                            .addOnSuccessListener { cont.resume(it.bitmap) }
                                            .addOnFailureListener { cont.resume(null) }
                                    }
                                }.getOrNull()
                            }
                            _photos.value = bitmaps
                            _isLoadingPhotos.value = false
                        }
                    }
                    .addOnFailureListener {
                        Log.e("PLACES", "Error fetching place metadata", it)
                        _isLoadingPhotos.value = false
                    }
            }.onFailure {
                Log.e("PLACES", "Error loading photos", it)
                _isLoadingPhotos.value = false
            }
        }
    }
}