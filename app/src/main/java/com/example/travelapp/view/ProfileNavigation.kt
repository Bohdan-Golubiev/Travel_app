package com.example.travelapp.view

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class ProfileNavigation(val route: String) {

    data object Profile : ProfileNavigation("profile")

    data object ListOfRoutes : ProfileNavigation("list")

    data object Route : ProfileNavigation("route/{routeId}/{routeName}") {
        fun createRoute(routeId: String, routeName: String) =
            "route/$routeId/$routeName"

        val navArguments = listOf(
            navArgument("routeId") { type = NavType.StringType },
            navArgument("routeName") { type = NavType.StringType }
        )
    }

    data object Place : ProfileNavigation("place/{routeId}/{placeId}/{placeName}") {
        fun createRoute(routeId: String, placeId: String, placeName: String) =
            "place/$routeId/$placeId/$placeName"

        val navArguments = listOf(
            navArgument("routeId") { type = NavType.StringType },
            navArgument("placeId") { type = NavType.StringType },
            navArgument("placeName") { type = NavType.StringType }
        )
    }

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

    data object Payment : ProfileNavigation("payment")
}