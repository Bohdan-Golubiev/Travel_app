package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application),application)

    private val _place = MutableStateFlow<PlaceEntity?>(null)
    val place: StateFlow<PlaceEntity?> = _place.asStateFlow()

    fun loadPlace(placeId: String, routeId: String) {
        viewModelScope.launch {
            repository.getPlaces(routeId)
                .collect { places ->
                    _place.value = places.find { it.id == placeId }
                }
        }
    }
}