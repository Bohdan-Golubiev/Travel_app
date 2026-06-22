package com.example.travelapp.viewmodel.create.routes

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.BuildConfig
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.repository.BookingRepository
import com.example.travelapp.data.repository.HotelRepository
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.map
import com.example.travelapp.viewmodel.create.SearchError
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RouteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)
    private val bookingRepository = BookingRepository(TravelDB.getInstance(application), application)
    private val hotelRepository = HotelRepository(TravelDB.getInstance(application), application)
    private val placesClient: PlacesClient
    init {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(application, BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(application)
    }

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editedName = MutableStateFlow("")
    val editedName: StateFlow<String> = _editedName.asStateFlow()

    private val _editedDescription = MutableStateFlow("")
    val editedDescription: StateFlow<String> = _editedDescription.asStateFlow()

    private val _editedPlaces = MutableStateFlow<List<PlaceEntity>>(emptyList())
    val editedPlaces: StateFlow<List<PlaceEntity>> = _editedPlaces.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _timelineError = MutableStateFlow(false)
    val timelineError: StateFlow<Boolean> = _timelineError.asStateFlow()

    private val _editedIsFavorite = MutableStateFlow(false)
    val editedIsFavorite: StateFlow<Boolean> = _editedIsFavorite.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val suggestions: StateFlow<List<AutocompletePrediction>> = _suggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<SearchError?>(null)
    val searchError: StateFlow<SearchError?> = _searchError.asStateFlow()

    private var searchJob: Job? = null

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val placeDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    fun getPlaces(routeId: String): Flow<List<PlaceEntity>> =
        repository.getPlaces(routeId)

    fun getBookingsForRoute(userId: String, routeId: String): Flow<List<BookingEntity>> =
        bookingRepository.getBookings(userId).map { list -> list.filter { it.routeId == routeId } }

    fun getHotelsForRoute(userId: String, routeId: String): Flow<List<HotelEntity>> =
        hotelRepository.getHotelsByUser(userId).map { list -> list.filter { it.routeId == routeId } }

    fun startEditing(
        currentName: String,
        currentDescription: String,
        currentPlaces: List<PlaceEntity>,
        routeId: String
    ) {
        viewModelScope.launch {
            val route = repository.getRoute(routeId)
            _editedName.value = currentName
            _editedDescription.value = currentDescription
            _editedPlaces.value = currentPlaces
            _editedIsFavorite.value = route?.isFavorite ?: false
            _isEditing.value = true
        }
    }

    fun onFavoriteToggle() {
        _editedIsFavorite.update { !it }
    }

    fun cancelEditing() {
        _isEditing.value = false
        clearSearch()
    }

    fun onNameChange(name: String) {
        _editedName.update { name }
    }

    fun onDescriptionChange(description: String) {
        _editedDescription.update { description }
    }

    fun movePlace(from: Int, to: Int) {
        _editedPlaces.update { list ->
            if (from < 0 || to < 0 || from >= list.size || to >= list.size) return@update list
            list.toMutableList().also { it.add(to, it.removeAt(from)) }
        }
    }

    fun removePlace(index: Int) {
        _editedPlaces.update { list ->
            list.toMutableList().also { it.removeAt(index) }
        }
    }
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _searchError.value = null
        searchJob?.cancel()

        if (query.length < 2) {
            _suggestions.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            if (!isNetworkAvailable()) {
                _suggestions.value = emptyList()
                _isSearching.value = false
                _searchError.value = SearchError.NO_INTERNET
                return@launch
            }
            _isSearching.value = true
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .build()
                val response = placesClient.findAutocompletePredictions(request).await()
                _suggestions.value = response.autocompletePredictions
                _isSearching.value = false
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                _suggestions.value = emptyList()
                _isSearching.value = false
                _searchError.value = SearchError.INVALID_REQUEST
            }
        }
    }

    fun onSuggestionSelected(prediction: AutocompletePrediction, routeId: String) {
        viewModelScope.launch {
            val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
            val result = runCatching { placesClient.fetchPlace(request).await() }
            val latLng = result.getOrNull()?.place?.latLng

            val newPlace = PlaceEntity(
                id = UUID.randomUUID().toString(),
                googlePlaceId = prediction.placeId,
                routeId = routeId,
                name = prediction.getPrimaryText(null).toString(),
                location = prediction.getSecondaryText(null).toString(),
                orderInRoute = _editedPlaces.value.size,
                visitDate = "",
                latitude = latLng?.latitude,
                longitude = latLng?.longitude
            )
            _editedPlaces.update { it + newPlace }
            _suggestions.value = emptyList()
            _searchQuery.value = ""
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _suggestions.value = emptyList()
        _isSearching.value = false
        _searchError.value = null
        searchJob?.cancel()
    }

    fun clearError() {
        _searchError.value = null
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun saveChanges(
        userId: String,
        routeId: String,
        originalPlaces: List<PlaceEntity>,
        onSuccess: (String, String) -> Unit
    ) {
        if (!checkCorrectTimeLine()) {
            _timelineError.value = true
            return
        }
        _timelineError.value = false
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cleanedDescription = _editedDescription.value
                    .replace(Regex("\n{3,}"), "\n\n").trim()

                repository.updateRoute(
                    userId = userId,
                    routeId = routeId,
                    newName = _editedName.value,
                    newDescription = cleanedDescription,
                    isFavorite = _editedIsFavorite.value
                )

                val editedIds = _editedPlaces.value.map { it.id }.toSet()
                val deletedPlaces = originalPlaces.filter { it.id !in editedIds }

                deletedPlaces.forEach { place ->
                    cancelLocationReminder(place)
                    repository.deletePlace(userId, routeId, place.id)
                }

                val originalIds = originalPlaces.map { it.id }.toSet()
                _editedPlaces.value.forEachIndexed { index, place ->
                    if (place.id !in originalIds) {
                        repository.addPlace(
                            place = place.copy(orderInRoute = index),
                            userId = userId
                        )
                    }
                }

                _editedPlaces.value.forEachIndexed { index, place ->
                    if (place.orderInRoute != index)
                        repository.updatePlaceOrder(place.id, index, userId, routeId)
                }
                _editedPlaces.value.forEachIndexed { index, place ->
                    repository.updatePlaceDate(place.id, index, place.visitDate, userId, routeId)
                }

                _editedPlaces.value.forEach { place -> scheduleLocationReminderIfFuture(place) }

                _isEditing.value = false
                clearSearch()
                onSuccess(_editedName.value, cleanedDescription)
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePlaceDate(index: Int, date: String) {
        _editedPlaces.update { list ->
            list.toMutableList().also {
                it[index] = it[index].copy(visitDate = date)
            }
        }
    }

    fun checkCorrectTimeLine(): Boolean {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val parsedDates = _editedPlaces.value.map { place ->
            val dateStr = place.visitDate
            if (dateStr.isEmpty() || dateStr == "00.00.0000") return false
            val parsed = runCatching { dateFormat.parse(dateStr) }.getOrNull()
                ?: return false
            parsed
        }
        return parsedDates
            .zipWithNext()
            .all { (prev, next) -> !next.before(prev) }
    }

    fun dismissTimelineError() {
        _timelineError.value = false
    }

    private fun scheduleLocationReminderIfFuture(place: PlaceEntity) {
        val visitMs = parsePlaceDate(place.visitDate) ?: return
        val startOfToday = System.currentTimeMillis().let {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        if (visitMs < startOfToday) return
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
        Log.d("DeleteAlarm", "Видалено ${place.name}")
    }
}