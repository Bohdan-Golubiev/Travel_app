package com.example.travelapp

import com.example.travelapp.model.dataclasses.AviationstackResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface AviationstackService {
    @GET("v1/flights")
    suspend fun searchFlights(
        @Query("access_key") accessKey: String,
        @Query("dep_iata") depIata: String,
        @Query("arr_iata") arrIata: String,
        @Query("limit") limit: Int = 10
    ): AviationstackResponse
}