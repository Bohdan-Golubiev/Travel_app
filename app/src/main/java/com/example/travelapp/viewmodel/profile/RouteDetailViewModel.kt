package com.example.travelapp.viewmodel.profile

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.notification.TravelAlarmManager
import com.example.travelapp.notification.removeAlarm
import com.example.travelapp.notification.saveAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RouteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)

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

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val placeDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    fun getPlaces(routeId: String): Flow<List<PlaceEntity>> =
        repository.getPlaces(routeId)

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
    }

    fun onNameChange(name: String) {
        _editedName.update { name }
    }

    fun onDescriptionChange(description: String) {
        _editedDescription.update { description }
    }

    fun movePlace(from: Int, to: Int) {
        _editedPlaces.update { list ->
            list.toMutableList().also { it.add(to, it.removeAt(from)) }
        }
    }

    fun removePlace(index: Int) {
        _editedPlaces.update { list ->
            list.toMutableList().also { it.removeAt(index) }
        }
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

                _editedPlaces.value.forEachIndexed { index, place ->
                    if (place.orderInRoute != index)
                        repository.updatePlaceOrder(place.id, index, userId, routeId)
                }
                _editedPlaces.value.forEachIndexed { index, place ->
                    repository.updatePlaceDate(place.id, index, place.visitDate, userId, routeId)
                }

                _editedPlaces.value.forEach { place -> scheduleLocationReminderIfFuture(place) }

                _isEditing.value = false
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
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        if (visitMs < startOfToday) return

        val alarmId = place.id.hashCode()
        TravelAlarmManager.scheduleLocationReminder(ctx, alarmId, visitMs)
        saveAlarm(ctx, alarmId, TravelAlarmManager.ReminderType.LOCATION, visitMs)
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