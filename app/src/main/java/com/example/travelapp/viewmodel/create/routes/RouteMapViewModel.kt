package com.example.travelapp.viewmodel.create.routes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.BuildConfig
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.repository.TravelRepository
import com.example.travelapp.db.TravelDB
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteSegmentInfo(
    val points: List<LatLng>,
    val distanceMeters: Int,
    val durationSeconds: Int
)
class RouteMapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TravelRepository(TravelDB.getInstance(application), application)

    fun getPlaces(routeId: String): Flow<List<PlaceEntity>> =
        repository.getPlaces(routeId)

    enum class TravelMode { DRIVING, WALKING }

    private val _travelMode = MutableStateFlow(TravelMode.DRIVING)
    val travelMode: StateFlow<TravelMode> = _travelMode.asStateFlow()

    private val _isLoadingRoute = MutableStateFlow(false)
    val isLoadingRoute: StateFlow<Boolean> = _isLoadingRoute.asStateFlow()
    private val _routeSegments = MutableStateFlow<List<RouteSegmentInfo>>(emptyList())
    val routeSegments: StateFlow<List<RouteSegmentInfo>> = _routeSegments.asStateFlow()

    private val _selectedSegmentIndex = MutableStateFlow<Int?>(null)
    val selectedSegmentIndex: StateFlow<Int?> = _selectedSegmentIndex.asStateFlow()

    fun onSegmentClick(index: Int) {
        _selectedSegmentIndex.value = if (_selectedSegmentIndex.value == index) null else index
    }

    fun dismissSegmentInfo() {
        _selectedSegmentIndex.value = null
    }

    fun setTravelMode(mode: TravelMode) {
        _travelMode.value = mode
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var shift = 0; var result = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0; result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            poly.add(LatLng(lat / 1e5, lng / 1e5))
        }
        return poly
    }

    fun fetchRoutePolylines(places: List<PlaceEntity>) {
        val validPlaces = places.filter { it.latitude != null && it.longitude != null }
        if (validPlaces.size < 2) {
            _routeSegments.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoadingRoute.value = true
            val segments = mutableListOf<RouteSegmentInfo>()
            val mode = if (_travelMode.value == TravelMode.DRIVING) "driving" else "walking"

            for (i in 0 until validPlaces.size - 1) {
                val origin = validPlaces[i]
                val dest = validPlaces[i + 1]
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${dest.latitude},${dest.longitude}" +
                        "&mode=$mode" +
                        "&key=${BuildConfig.MAPS_API_KEY}"

                try {
                    segments.add(fetchSegmentInfo(url))
                } catch (e: Exception) {
                    segments.add(RouteSegmentInfo(
                        points = listOf(
                            LatLng(origin.latitude!!, origin.longitude!!),
                            LatLng(dest.latitude!!, dest.longitude!!)
                        ),
                        distanceMeters = 0,
                        durationSeconds = 0
                    ))
                }
            }

            _routeSegments.value = segments
            _selectedSegmentIndex.value = null
            _isLoadingRoute.value = false
        }
    }

    private suspend fun fetchSegmentInfo(url: String): RouteSegmentInfo {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val json = org.json.JSONObject(response)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) throw Exception("No routes found")

            val route = routes.getJSONObject(0)
            val leg = route.getJSONArray("legs").getJSONObject(0)

            val distanceMeters = leg.getJSONObject("distance").getInt("value")
            val durationSeconds = leg.getJSONObject("duration").getInt("value")

            val points = decodePolyline(
                route.getJSONObject("overview_polyline").getString("points")
            )

            RouteSegmentInfo(points, distanceMeters, durationSeconds)
        }
    }
}