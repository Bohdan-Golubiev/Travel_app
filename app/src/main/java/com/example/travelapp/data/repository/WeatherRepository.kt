package com.example.travelapp.data.repository

import android.util.Log
import com.example.travelapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class WeatherInfo(
    val tempC: Double,
    val conditionText: String,
    val conditionIconUrl: String,
    val humidity: Int,
    val windKph: Double,
)

class WeatherRepository {

    private val apiKey = BuildConfig.WEATHER_API_KEY

    suspend fun getForecast(location: String, visitDate: String): WeatherInfo? {
        if (visitDate.isBlank()) return null

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val visit = runCatching { sdf.parse(visitDate) }.getOrNull() ?: return null
        val today = Date()

        val diffDays = TimeUnit.MILLISECONDS.toDays(visit.time - today.time)
        if (diffDays < 0 || diffDays > 10) return null

        val apiDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(visit)

        return withContext(Dispatchers.IO) {
            runCatching {
                val query = location.trim().replace(" ", "%20")
                val url = "https://api.weatherapi.com/v1/forecast.json" +
                        "?key=$apiKey&q=$query&dt=$apiDate&days=1&aqi=no&alerts=no"
                val response = URL(url).readText()
                parseWeather(response)
            }.onFailure {
                Log.e("WeatherRepository", "Error fetching weather", it)
            }.getOrNull()
        }
    }

    private fun parseWeather(json: String): WeatherInfo {
        val root = JSONObject(json)
        val day = root
            .getJSONObject("forecast")
            .getJSONArray("forecastday")
            .getJSONObject(0)
            .getJSONObject("day")

        val condition = day.getJSONObject("condition")

        return WeatherInfo(
            tempC = day.getDouble("avgtemp_c"),
            conditionText = condition.getString("text"),
            conditionIconUrl = "https:" + condition.getString("icon"),
            humidity = day.getInt("avghumidity"),
            windKph = day.getDouble("maxwind_kph")
        )
    }
}