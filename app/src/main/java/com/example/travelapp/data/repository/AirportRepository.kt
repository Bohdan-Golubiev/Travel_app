package com.example.travelapp.data.repository

import android.content.Context
import com.example.travelapp.data.entity.AirportEntity
import com.example.travelapp.db.TravelDB
import com.example.travelapp.model.dataclasses.Airport

class AirportRepository(
    private val context: Context,
    private val db: TravelDB
) {

    suspend fun syncAirportsFromAssetsIfNeeded() {
        val count = db.airportDao().count()
        if (count > 0) return

        val parsed = parseAirportsFromAssets()
        if (parsed.isNotEmpty()) {
            db.airportDao().insertAll(parsed)
        }
    }

    suspend fun searchByCity(query: String): List<Airport> {
        if (query.length < 2) return emptyList()
        val q = query.trim()
        return db.airportDao().searchByCity(q).map {
            Airport(
                name    = it.name,
                city    = it.municipality,
                country = it.iso_country,
                iata    = it.iata_code,
                icao    = it.icao_code
            )
        }
    }

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