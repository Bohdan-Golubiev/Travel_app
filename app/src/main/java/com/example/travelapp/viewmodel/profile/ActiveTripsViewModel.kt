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
    val nextPlace: PlaceEntity?,
    val progressPercent: Int = 0,
    val visitedCount: Int = 0,
    val totalCount: Int = 0
)

class ActiveTripsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelRepository(TravelDB.getInstance(application), application)

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val todayDate: Date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today) ?: Date()

    private fun parseDmY(dateStr: String): Date? = try {
        dateFormat.parse(dateStr)
    } catch (e: Exception) { null }

    private fun calculateProgress(places: List<PlaceEntity>): Int {
        if (places.isEmpty()) return 0
        val sorted = places.mapNotNull { p ->
            parseDmY(p.visitDate)?.let { Pair(it, p) }
        }.sortedBy { it.first }
        if (sorted.isEmpty()) return 0

        val firstDate = sorted.first().first
        val lastDate = sorted.last().first

        if (firstDate == lastDate) {
            return if (!todayDate.before(firstDate)) 100 else 0
        }

        return when {
            todayDate.before(firstDate) -> 0
            !todayDate.before(lastDate) -> 100
            else -> {
                val total = lastDate.time - firstDate.time
                val elapsed = todayDate.time - firstDate.time
                ((elapsed.toDouble() / total) * 100).toInt().coerceIn(0, 100)
            }
        }
    }

    private fun countVisited(places: List<PlaceEntity>): Int =
        places.count { p -> parseDmY(p.visitDate)?.let { !todayDate.before(it) } ?: false }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActiveTrips(userId: String): Flow<List<ActiveTripItem>> =
        repository.getActiveTrips(userId, today)
            .flatMapLatest { routes ->
                if (routes.isNullOrEmpty()) {
                    flowOf(emptyList())
                } else {
                    val perRoute = routes.map { route ->
                        combine(
                            repository.getNextPlaceForRoute(route.id, today),
                            repository.getPlaces(route.id)
                        ) { nextPlace, allPlaces ->
                            val progress = calculateProgress(allPlaces)
                            val visited = countVisited(allPlaces)
                            ActiveTripItem(
                                route = route,
                                nextPlace = nextPlace,
                                progressPercent = progress,
                                visitedCount = visited,
                                totalCount = allPlaces.size
                            )
                        }
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