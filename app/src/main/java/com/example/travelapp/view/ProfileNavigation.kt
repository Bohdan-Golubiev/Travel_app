package com.example.travelapp.view

sealed class ProfileNavigation(val route: String) {

    data object Profile : ProfileNavigation("profile")

    data object ListOfRoutes : ProfileNavigation("list")

    data object Route : ProfileNavigation("route/{routeId}/{routeName}") {
        fun createRoute(routeId: Int, routeName: String) =
            "route/$routeId/${routeName}"

        val navArguments = listOf(
            androidx.navigation.navArgument("routeId") {
                type = androidx.navigation.NavType.IntType
            },
            androidx.navigation.navArgument("routeName") {
                type = androidx.navigation.NavType.StringType
            }
        )
    }

    data object Place : ProfileNavigation("place/{placeId}/{placeName}") {
        fun createRoute(placeId: Int, placeName: String) =
            "place/$placeId/${placeName}"

        val navArguments = listOf(
            androidx.navigation.navArgument("placeId") {
                type = androidx.navigation.NavType.IntType
            },
            androidx.navigation.navArgument("placeName") {
                type = androidx.navigation.NavType.StringType
            }
        )
    }

    data object Booking : ProfileNavigation("booking")

    data object BookingDetail : ProfileNavigation("booking_detail/{bookingId}/{bookingName}") {
        fun createRoute(bookingId: Int, bookingName: String) =
            "booking_detail/$bookingId/${bookingName}"

        val navArguments = listOf(
            androidx.navigation.navArgument("bookingId") {
                type = androidx.navigation.NavType.IntType
            },
            androidx.navigation.navArgument("bookingName") {
                type = androidx.navigation.NavType.StringType
            }
        )
    }

    data object Review : ProfileNavigation("review")

    data object Payment : ProfileNavigation("payment")
}