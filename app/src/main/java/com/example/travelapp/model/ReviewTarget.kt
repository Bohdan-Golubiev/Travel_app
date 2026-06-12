package com.example.travelapp.model

import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity

sealed class ReviewTarget {
    abstract val googlePlaceId: String
    abstract val name: String
    abstract val subtitle: String
    abstract val targetType: String

    data class Place(val entity: PlaceEntity) : ReviewTarget() {
        override val googlePlaceId = entity.googlePlaceId
        override val name          = entity.name
        override val subtitle      = entity.location
        override val targetType    = "place"
    }

    data class Hotel(val entity: HotelEntity) : ReviewTarget() {
        override val googlePlaceId = entity.id.removeSuffix(entity.routeId)
        override val name          = entity.name
        override val subtitle      = entity.address
        override val targetType    = "hotel"
    }

    data class Booking(val entity: BookingEntity) : ReviewTarget() {
        override val googlePlaceId = entity.id.removeSuffix(entity.routeId.uppercase())
        override val name          = entity.name
        override val subtitle      = entity.date
        val from                   = entity.from
        val to                     = entity.to
        override val targetType    = "booking"
    }
}