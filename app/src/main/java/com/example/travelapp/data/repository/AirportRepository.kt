package com.example.travelapp.data.repository

import android.content.Context
import com.example.travelapp.model.dataclasses.Airport

class AirportRepository(private val context: Context) {

    private val airports: List<Airport> by lazy { loadAirports() }

    private fun loadAirports(): List<Airport> {
        val result = mutableListOf<Airport>()

        context.assets.open("airports.csv").bufferedReader().use { reader ->
            reader.readLine() // пропускаємо header рядок

            reader.forEachLine { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 14) return@forEachLine

                // 0:id, 1:ident, 2:type, 3:name, 4:lat, 5:lon,
                // 6:elevation, 7:continent, 8:iso_country, 9:iso_region,
                // 10:municipality, 11:scheduled_service,
                // 12:icao_code, 13:iata_code, 14:gps_code, ...

                val iata = parts[13].trim()
                val icao = parts[12].trim()
                val city = parts[10].trim()
                val name = parts[3].trim()
                val country = parts[8].trim()

                if (iata.isBlank() || city.isBlank()) return@forEachLine

                result.add(
                    Airport(
                        name    = name,
                        city    = city,
                        country = country,
                        iata    = iata,
                        icao    = icao
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

    fun searchByCity(query: String): List<Airport> {
        if (query.length < 2) return emptyList()
        val q = query.lowercase().trim()
        return airports
            .filter { it.city.lowercase().contains(q) }
            .take(5)
    }
}