package com.example.travelapp.viewmodel.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val RAPIDAPI_HOST = "xotelo-hotel-prices.p.rapidapi.com"
private const val BASE_URL = "https://xotelo-hotel-prices.p.rapidapi.com"

data class HotelResult(
    val hotelKey: String,
    val name: String,
    val address: String = "",
    val imageUrl: String = "",
    val cost: Double = 100.0
)

data class SelectedHotelEntry(
    val hotel    : HotelResult,
    val dateFrom : String,
    val dateTo   : String,
    val days     : Int,
    val totalCost: Double
)

data class HotelItemState(
    val dateFrom   : String  = "",
    val dateTo     : String  = "",
    val isExpanded : Boolean = false
) {
    val days: Int get() {
        val from = parseDateMillis(dateFrom)
        val to   = parseDateMillis(dateTo)
        return if (from != null && to != null) daysBetween(from, to) else 0
    }
}

sealed class SearchState {
    object Idle    : SearchState()
    object Loading : SearchState()
    data class Error(val message: String) : SearchState()
    data class Success(val hotels: List<HotelResult>) : SearchState()
}

data class FindHotelUiState(
    val startPlace     : String                      = "",
    val searchState    : SearchState                 = SearchState.Idle,
    val itemStates     : Map<String, HotelItemState> = emptyMap(),
    val selectedHotels : List<SelectedHotelEntry>    = emptyList(),
)

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

class FindHotelViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FindHotelUiState())
    val uiState: StateFlow<FindHotelUiState> = _uiState.asStateFlow()

    fun onStartPlaceChange(value: String) {
        _uiState.update { it.copy(startPlace = value) }
    }

    fun searchHotels() {
        val query = _uiState.value.startPlace.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(searchState = SearchState.Loading) }
            try {
                val hotels = withContext(Dispatchers.IO) { fetchHotelList(query) }
                val itemStates = hotels.associate { it.hotelKey to HotelItemState() }
                _uiState.update {
                    it.copy(
                        searchState = SearchState.Success(hotels),
                        itemStates  = itemStates
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(searchState = SearchState.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    private fun fetchHotelList(location: String): List<HotelResult> {
        val encoded = URLEncoder.encode(location, "UTF-8")
        val url = "$BASE_URL/api/search?location_type=accommodation&query=$encoded"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", BuildConfig.RAPIDAPI_KEY)
            .addHeader("x-rapidapi-host", RAPIDAPI_HOST)
            .addHeader("Content-Type", "application/json")
            .build()

        val body = httpClient.newCall(request).execute().use { response ->
            val text = response.body?.string()
                ?: throw Exception("Empty response from server")
            if (!response.isSuccessful) {
                val msg = runCatching {
                    JSONObject(text).optString("message", "HTTP ${response.code}")
                }.getOrDefault("HTTP ${response.code}")
                throw Exception(msg)
            }
            text
        }

        val parsed = parseHotelList(body)

        return parsed.filter {
            val city = it.address.substringBefore(",").trim()
            city.equals(location, ignoreCase = true) ||
                    it.address.contains(location, ignoreCase = true)
        }
    }

    fun toggleExpand(hotelKey: String) {
        _uiState.update { state ->
            val updated = state.itemStates.toMutableMap()
            val current = updated[hotelKey] ?: HotelItemState()
            updated[hotelKey] = current.copy(isExpanded = !current.isExpanded)
            state.copy(itemStates = updated)
        }
    }

    fun onDateFromSelected(hotelKey: String, millis: Long) {
        _uiState.update { state ->
            val updated = state.itemStates.toMutableMap()
            val current = updated[hotelKey] ?: HotelItemState()
            updated[hotelKey] = current.copy(dateFrom = formatDate(millis))
            state.copy(itemStates = updated)
        }
    }

    fun onDateToSelected(hotelKey: String, millis: Long) {
        _uiState.update { state ->
            val updated = state.itemStates.toMutableMap()
            val current = updated[hotelKey] ?: HotelItemState()
            updated[hotelKey] = current.copy(dateTo = formatDate(millis))
            state.copy(itemStates = updated)
        }
    }

    fun onAddClick(hotel: HotelResult) {
        val itemState = _uiState.value.itemStates[hotel.hotelKey] ?: return
        if (itemState.days <= 0) return

        val entry = SelectedHotelEntry(
            hotel     = hotel,
            dateFrom  = itemState.dateFrom,
            dateTo    = itemState.dateTo,
            days      = itemState.days,
            totalCost = itemState.days * hotel.cost
        )

        _uiState.update { state ->
            val existing = state.selectedHotels.toMutableList()
            val idx = existing.indexOfFirst { it.hotel.hotelKey == hotel.hotelKey }
            if (idx >= 0) existing[idx] = entry else existing.add(entry)
            state.copy(selectedHotels = existing)
        }
    }

    fun onRemoveSelected(hotelKey: String) {
        _uiState.update { state ->
            state.copy(selectedHotels = state.selectedHotels.filter { it.hotel.hotelKey != hotelKey })
        }
    }
}

private fun parseHotelList(json: String): List<HotelResult> {
    val root = JSONObject(json)

    val array = when {
        root.has("result") -> {
            val result = root.getJSONObject("result")
            when {
                result.has("list") -> result.getJSONArray("list")
                result.has("data") -> result.getJSONArray("data")
                else -> throw Exception("Unexpected JSON: $json")
            }
        }
        root.has("data") -> root.getJSONArray("data")
        else -> throw Exception("Unexpected JSON: $json")
    }

    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val key = item.optString("key", item.optString("hotel_key", ""))
            if (key.isBlank()) continue
            add(
                HotelResult(
                    hotelKey = key,
                    name     = item.optString("name", "Hotel"),
                    address  = item.optString("place_name", item.optString("place_name", "")),
                    imageUrl = item.optString("image", item.optString("photo", ""))
                )
            )
        }
    }
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))

fun parseDateMillis(date: String): Long? = runCatching {
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date)?.time
}.getOrNull()

fun toApiDate(ddMMyyyy: String): String? = runCatching {
    val from = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val to   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    to.format(from.parse(ddMMyyyy)!!)
}.getOrNull()

fun daysBetween(fromMillis: Long, toMillis: Long): Int {
    val diff = toMillis - fromMillis
    return if (diff > 0) (diff / (1000 * 60 * 60 * 24)).toInt() else 0
}