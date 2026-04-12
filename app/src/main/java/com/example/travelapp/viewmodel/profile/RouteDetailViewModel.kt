package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editedName = MutableStateFlow("")
    val editedName: StateFlow<String> = _editedName.asStateFlow()

    private val _editedPlaces = MutableStateFlow<List<PlaceEntity>>(emptyList())
    val editedPlaces: StateFlow<List<PlaceEntity>> = _editedPlaces.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getPlaces(routeId: String): Flow<List<PlaceEntity>> =
        repository.getPlaces(routeId)

    fun startEditing(currentName: String, currentPlaces: List<PlaceEntity>) {
        _editedName.value = currentName
        _editedPlaces.value = currentPlaces
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun onNameChange(name: String) {
        _editedName.update { name }
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

    fun saveChanges(userId: String, routeId: String, originalPlaces: List<PlaceEntity>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateRouteName(userId, routeId, _editedName.value)

                val editedIds = _editedPlaces.value.map { it.id }.toSet()
                val deletedPlaces = originalPlaces.filter { it.id !in editedIds }
                deletedPlaces.forEach { place ->
                    repository.deletePlace(userId, routeId, place.id)
                }

                _editedPlaces.value.forEachIndexed { index, place ->
                    if (place.orderInRoute != index) {
                        repository.updatePlaceOrder(place.id, index, userId, routeId)
                    }
                }

                _editedPlaces.value.forEachIndexed { index, place ->
                    repository.updatePlaceDate(place.id, index, place.visitDate, userId, routeId)
                }

                _isEditing.value = false
                onSuccess()
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
}