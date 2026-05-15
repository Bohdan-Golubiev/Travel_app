package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RoutesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)

    fun getRoutes(userId: String): Flow<List<RouteEntity>> =
        repository.getRoutes(userId)

    fun deleteRoute(userId: String, routeId: String) {
        viewModelScope.launch {
            repository.deleteRoute(userId, routeId)
        }
    }

    fun setRouteCompleted(routeId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setRouteCompleted(routeId, isCompleted)
        }
    }
}