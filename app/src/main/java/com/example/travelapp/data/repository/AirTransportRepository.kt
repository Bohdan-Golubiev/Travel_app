package com.example.travelapp.data.repository

import com.example.travelapp.AviationstackService
import com.example.travelapp.model.dataclasses.FlightData
import com.example.travelapp.view.create.BookingOption
import com.example.travelapp.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class AirTransportRepository {

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.aviationstack.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(AviationstackService::class.java)
    suspend fun searchFlights(
        from: String,
        to: String,
    ): List<BookingOption> {
        val response = service.searchFlights(
            accessKey = BuildConfig.AVIATIONSTACK_KEY,
            depIata = from.uppercase(),
            arrIata = to.uppercase(),
        )

        return response.data.mapNotNull { it.toBookingOption() }
    }

    private fun FlightData.toBookingOption(): BookingOption? {

        val flightCode = flight.iata ?: return null
        if (!isValidFlightCode(flightCode)) return null

        val name = "${airline.name ?: "Unknown"} · $flightCode".trim()

        val depTime = departure.scheduled
            ?.substringAfter("T")
            ?.substring(0, 5)
            ?: "—"

        val from = departure.timezone
            ?.substringAfter("/")
            ?: "Unknown"

        val arrTime = arrival.scheduled
            ?.substringAfter("T")
            ?.substring(0, 5)
            ?: "—"

        val to = arrival.timezone
            ?.substringAfter("/")
            ?: "Unknown"

        val date = departure.scheduled //"scheduled":"2026-04-15T17:00:00+00:00"
            ?.substring(0, 10)
            ?.split("-")
            ?.let { "${it[2]}.${it[1]}.${it[0]}" }

        val fromIATA = " (" + departure.iata + ")"
        val toIATA = " (" +  arrival.iata + ")"

        return BookingOption(
            name = name,
            time = "$depTime → $arrTime",
            date = "$date",
            cost = if (name.length < 10) name.length * 180.0
            else name.length * 120.0,
            status = flight_status,
            from = from + fromIATA,
            to = to + toIATA
        )
    }
    private fun isValidFlightCode(code: String): Boolean {
        val regex = Regex("^[A-Z]{1,3}\\d{1,5}$")
        return regex.matches(code)
    }
}