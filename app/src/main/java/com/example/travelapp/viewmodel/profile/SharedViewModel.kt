package com.example.travelapp.viewmodel.profile

import androidx.lifecycle.ViewModel
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.model.ReviewItem
import com.example.travelapp.model.ReviewTarget
import com.example.travelapp.view.create.BookingOption
import com.example.travelapp.viewmodel.create.SelectedHotelEntry

class SharedViewModel : ViewModel() {
    var selectedPlace: PlaceEntity? = null
    var selectedBooking: BookingEntity? = null
    var selectedHotel: HotelEntity? = null
    var selectedReview: ReviewItem? = null

    var pendingRouteId: String = ""
    var pendingBookedVehicles: List<BookingOption> = emptyList()
    var pendingBookedHotels: List<SelectedHotelEntry> = emptyList()

    fun setReviewTarget(target: ReviewTarget) {
        selectedPlace = null
        selectedHotel = null
        selectedBooking = null

        when (target) {
            is ReviewTarget.Place   -> selectedPlace = target.entity
            is ReviewTarget.Hotel   -> selectedHotel = target.entity
            is ReviewTarget.Booking -> selectedBooking = target.entity
        }
    }
}