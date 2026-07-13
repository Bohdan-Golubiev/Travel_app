package com.example.travelapp.view

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

sealed class ProfileNavigation(val route: String) {

    data object Profile : ProfileNavigation("profile")

    data object AddReview : ProfileNavigation("add_review")

    data object Booking : ProfileNavigation("booking")

    data object BookingDetail : ProfileNavigation("booking_detail/{bookingId}/{bookingName}") {

        fun createRoute(bookingId: String, bookingName: String) =
            "booking_detail/$bookingId/$bookingName"

        val navArguments = listOf(
            navArgument("bookingId") { type = NavType.StringType },
            navArgument("bookingName") { type = NavType.StringType }
        )
    }

    data object Hotel : ProfileNavigation("hotel")

    data object HotelDetail : ProfileNavigation("hotel_detail/{hotelId}/{hotelName}") {

        fun createRoute(hotelId: String, hotelName: String) =
            "hotel_detail/$hotelId/$hotelName"

        val navArguments = listOf(
            navArgument("hotelId") { type = NavType.StringType },
            navArgument("hotelName") { type = NavType.StringType }
        )
    }

    data object Review : ProfileNavigation("review")

    data object EditReview : ProfileNavigation("edit_review")

    data object ActiveTrips : ProfileNavigation("active_trips") {
        val deepLinks = listOf(
            navDeepLink { uriPattern = "travelapp://trips/active_trips" }
        )
    }

    data object SpendingStats : ProfileNavigation("spending_stats")
}