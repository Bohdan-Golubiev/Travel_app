package com.example.travelapp.model.dataclasses

data class AviationstackResponse(
    val data: List<FlightData>
)

data class FlightData(
    val flight_date: String,
    val flight_status: String,
    val departure: AirportInfo,
    val arrival: AirportInfo,
    val airline: AirlineInfo,
    val flight: FlightInfo
)

data class AirportInfo(
    val airport: String?,
    val iata: String?,
    val scheduled: String?,
    val estimated: String?,
    val actual: String?
)

data class AirlineInfo(
    val name: String?
)

data class FlightInfo(
    val iata: String?
)