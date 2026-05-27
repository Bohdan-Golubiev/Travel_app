package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class ActiveTripItem(
    val route: RouteEntity,
    val nextPlace: PlaceEntity?
)

class ActiveTripsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActiveTrips(userId: String): Flow<List<ActiveTripItem>> =
        repository.getActiveTrips(userId, today)
            .flatMapLatest { routes ->
                if (routes.isNullOrEmpty()) {
                    flowOf(emptyList())
                } else {
                    val perRoute = routes.map { route ->
                        repository.getNextPlaceForRoute(route.id, today)
                            .map { nextPlace -> ActiveTripItem(route, nextPlace) }
                    }
                    combine(perRoute) { it.toList() }
                }
            }
            .map { items ->
                items.sortedBy { item ->
                    item.nextPlace?.visitDate
                        ?.let { date ->
                            val parts = date.split(".")
                            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
                        }
                        ?: "9999-99-99"
                }
            }
}