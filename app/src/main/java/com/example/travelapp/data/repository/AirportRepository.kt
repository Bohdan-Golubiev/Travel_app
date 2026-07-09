package com.example.travelapp.data.repository

import android.content.Context
import com.example.travelapp.data.entity.AirportEntity
import com.example.travelapp.db.TravelDB
import com.example.travelapp.model.dataclasses.Airport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.Normalizer

class AirportRepository(
    private val context: Context,
    private val db: TravelDB
) {
    @Volatile
    private var airports: List<Airport> = emptyList()
    private var isInitialized = false
    private val initMutex = Mutex()

    suspend fun initialize() {
        if (isInitialized) return
        initMutex.withLock {
            if (isInitialized) return
            withContext(Dispatchers.IO) {
                syncAirportsFromAssetsIfNeeded()
                airports = db.airportDao().getAll().map { it.toAirport() }
            }
            isInitialized = true
        }
    }

    suspend fun syncAirportsFromAssetsIfNeeded() {
        val count = db.airportDao().count()
        if (count > 0) return

        val parsed = parseAirportsFromAssets()
        if (parsed.isNotEmpty()) {
            db.airportDao().insertAll(parsed)
        }
    }

    suspend fun searchByCity(query: String): List<Airport> = withContext(Dispatchers.Default) {
        if (query.length < 2) return@withContext emptyList()
        val q = normalize(query)

        val exactMatches = airports.filter { normalize(it.city).contains(q) }
        if (exactMatches.isNotEmpty()) {
            return@withContext exactMatches
                .sortedWith(
                    compareBy(
                        { !normalize(it.city).startsWith(q) }, // startsWith вище за просто contains
                        { it.city }
                    )
                )
                .take(5)
        }

        val maxDistance = fuzzyThresholdFor(q.length)
        airports
            .map { airport -> airport to levenshtein(q, normalize(airport.city)) }
            .filter { (_, distance) -> distance <= maxDistance }
            .sortedBy { it.second }
            .take(5)
            .map { it.first }
    }

    private fun fuzzyThresholdFor(length: Int): Int = when {
        length <= 3 -> 1
        length <= 6 -> 2
        else -> 3
    }

    private fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "") // прибираємо діакритику (é -> e тощо)
            .lowercase()
            .trim()

    private fun levenshtein(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // видалення
                    dp[i][j - 1] + 1,      // вставка
                    dp[i - 1][j - 1] + cost // заміна
                )
            }
        }
        return dp[a.length][b.length]
    }

    private fun AirportEntity.toAirport() = Airport(
        name    = name,
        city    = municipality,
        country = iso_country,
        iata    = iata_code,
        icao    = icao_code
    )

    private fun parseAirportsFromAssets(): List<AirportEntity> {
        val result = mutableListOf<AirportEntity>()

        context.assets.open("airports.csv").bufferedReader().use { reader ->
            reader.readLine()

            reader.forEachLine { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 14) return@forEachLine

                val iata = parts[13].trim()
                val icao = parts[12].trim()
                val municipality = parts[10].trim()
                val name = parts[3].trim()

                if (iata.isBlank() || municipality.isBlank()) return@forEachLine

                result.add(
                    AirportEntity(
                        id           = parts[0].trim().toIntOrNull() ?: return@forEachLine,
                        ident        = parts[1].trim(),
                        type         = parts[2].trim(),
                        name         = name,
                        latitude     = parts[4].trim().toDoubleOrNull() ?: 0.0,
                        longitude    = parts[5].trim().toDoubleOrNull() ?: 0.0,
                        elevation_ft = parts[6].trim(),
                        continent    = parts[7].trim(),
                        iso_country  = parts[8].trim(),
                        iso_region   = parts[9].trim(),
                        municipality = municipality,
                        icao_code    = icao,
                        iata_code    = iata,
                        gps_code     = if (parts.size > 14) parts[14].trim() else ""
                    )
                )
            }
        }
        return result
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }
}