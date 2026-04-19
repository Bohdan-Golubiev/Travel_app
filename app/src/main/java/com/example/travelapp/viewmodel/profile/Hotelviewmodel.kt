package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.repository.HotelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HotelRepository(
        db = TravelDB.getInstance(application),
        context = application
    )
    private val dao = TravelDB.getInstance(application).hotelDao()

    fun getHotelsByUser(userId: String): Flow<List<HotelWithRoute>> =
        dao.getHotelWithRoute(userId)

    fun getHotelWithRouteById(hotelId: String): Flow<HotelWithRoute?> =
        dao.getByIdWithRoute(hotelId)

    fun deleteHotel(userId: String, hotel: HotelEntity) {
        viewModelScope.launch {
            repository.deleteHotel(userId, hotel)
        }
    }
}