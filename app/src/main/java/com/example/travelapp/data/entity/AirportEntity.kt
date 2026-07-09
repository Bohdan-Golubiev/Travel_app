package com.example.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airport")
data class AirportEntity(
    @PrimaryKey
    val id: Int,
    val ident: String,
    val type: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation_ft: String,
    val continent: String,
    val iso_country: String,
    val iso_region: String,
    val municipality: String,
    val icao_code: String,
    val iata_code: String,
    val gps_code: String,
)