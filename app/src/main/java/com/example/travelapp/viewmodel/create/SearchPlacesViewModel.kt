package com.example.travelapp.viewmodel.create

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.travelapp.BuildConfig
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

data class PlaceItem(
    val id: String,
    val googlePlaceId: String,
    val name: String,
    val location: String,
    val visitDate: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class SearchPlacesUiState(
    val routeName: String = "",
    val routeDescription: String = "",
    val searchQuery: String = "",
    val places: List<PlaceItem> = emptyList(),
    val suggestions: List<AutocompletePrediction> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SearchPlacesViewModel(application: Application) : AndroidViewModel(application) {

    private val placesClient: PlacesClient
    private val repository = TravelRepository(TravelDB.getInstance(application),application)
    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val placeDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    init {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(application, BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(application)
    }

    private val _uiState = MutableStateFlow(SearchPlacesUiState())
    val uiState: StateFlow<SearchPlacesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onRouteNameChange(name: String) {
        _uiState.update { it.copy(routeName = name) }
    }

    fun onRouteDescriptionChange(description: String) {
        _uiState.update { it.copy(routeDescription = description) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }

        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            if (!isNetworkAvailable()) {
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        isSearching = false,
                        errorMessage = "Немає підключення до інтернету"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSearching = true) }
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .build()

                val response = placesClient
                    .findAutocompletePredictions(request)
                    .await()

                _uiState.update {
                    it.copy(
                        suggestions = response.autocompletePredictions,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        isSearching = false,
                        errorMessage = "Помилка пошуку: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onSuggestionSelected(prediction: AutocompletePrediction) {
        viewModelScope.launch {
            val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
            val result = runCatching { placesClient.fetchPlace(request).await() }
            val latLng = result.getOrNull()?.place?.latLng

            val newPlace = PlaceItem(
                id = UUID.randomUUID().toString(),
                googlePlaceId = prediction.placeId,
                name = prediction.getPrimaryText(null).toString(),
                location = prediction.getSecondaryText(null).toString(),
                latitude = latLng?.latitude,
                longitude = latLng?.longitude
            )
            _uiState.update {
                it.copy(
                    places = it.places + newPlace,
                    suggestions = emptyList(),
                    searchQuery = ""
                )
            }
        }
    }

    fun removePlace(index: Int) {
        _uiState.update { state ->
            state.copy(places = state.places.toMutableList().also { it.removeAt(index) })
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun movePlace(from: Int, to: Int) {
        _uiState.update { state ->
            val list = state.places.toMutableList()
            list.add(to, list.removeAt(from))
            state.copy(places = list)
        }
    }
    fun updatePlaceDate(index: Int, date: String) {
        _uiState.update { state ->
            val list = state.places.toMutableList()
            list[index] = list[index].copy(visitDate = date)
            state.copy(places = list)
        }
    }

    fun saveRoute(userId: String, onSuccess: (String) -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val route = repository.createRoute(
                    userId = userId,
                    name = state.routeName,
                    description = state.routeDescription
                )
                state.places.forEachIndexed { index, placeItem ->
                    repository.addPlace(
                        place = PlaceEntity(
                            id = placeItem.id,
                            googlePlaceId = placeItem.googlePlaceId,
                            routeId = route.id,
                            name = placeItem.name,
                            location = placeItem.location,
                            latitude = placeItem.latitude,
                            longitude = placeItem.longitude,
                            orderInRoute = index,
                            visitDate = placeItem.visitDate
                        ),
                        userId = userId
                    )
                    setAlert(placeItem)
                }
                onSuccess(route.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Помилка збереження: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun setAlert(place: PlaceItem)
    {
        val visitMs = parsePlaceDate(place.visitDate) ?: return
        val alarmId = place.id.hashCode()

        TravelAlarmManager.scheduleLocationReminder(ctx, alarmId, visitMs)
    }
    private fun parsePlaceDate(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return runCatching { placeDateFormat.parse(dateStr)?.time }.getOrNull()
    }
    fun cancelLocationReminder(place: PlaceEntity) {
        val alarmId = place.id.hashCode()
        TravelAlarmManager.cancel(ctx, alarmId, TravelAlarmManager.ReminderType.LOCATION)
        removeAlarm(ctx, alarmId, TravelAlarmManager.ReminderType.LOCATION)
    }
}