package com.example.travelapp.model.dataclasses


data class Airport(
    val name: String,
    val city: String,
    val country: String,
    val iata: String,
    val icao: String
) {

    override fun toString() = "$city ($iata) — $name"
}