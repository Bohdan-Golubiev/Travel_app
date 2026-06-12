package com.example.travelapp.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.savedstate.SavedState
import com.example.travelapp.utils.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    route: String?,
    args: SavedState?,
    nav: NavHostController,
    canGoBack: Boolean,
    strings: AppStrings
) {
    val title = when (route) {
        CreateNavigation.ListOfRoutes.route ->
            strings.myRoutes

        CreateNavigation.Route.route ->
            args?.getString("routeName").orEmpty()

        CreateNavigation.Place.route ->
            args?.getString("placeName").orEmpty()

        CreateNavigation.AddReview.route ->
            strings.addReview

        CreateNavigation.FindVehicle.route ->
            strings.findVehicle

        CreateNavigation.FindHotel.route ->
            strings.findHotel

        CreateNavigation.HotelBooked.route ->
            strings.myBooking

        CreateNavigation.CreateRoute.route ->
            strings.createRoute

        ProfileNavigation.Booking.route ->
            strings.myBooking

        ProfileNavigation.BookingDetail.route ->
            args?.getString("bookingName").orEmpty()

        ProfileNavigation.Hotel.route ->
            strings.myHotels

        ProfileNavigation.HotelDetail.route ->
            args?.getString("hotelName").orEmpty()

        ProfileNavigation.Review.route ->
            strings.myReviews

        ProfileNavigation.EditReview.route ->
            strings.editReview

        ProfileNavigation.ActiveTrips.route ->
            strings.activeTrips

        ProfileNavigation.SpendingStats.route ->
            strings.statsSpending

        ProfileNavigation.AddReview.route ->
            strings.addReview

        else -> ""
    }

    TopAppBar(
        modifier = Modifier.height(80.dp),

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0D1B2A),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),

        title = {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },

        navigationIcon = {
            if (canGoBack) {
                IconButton(
                    modifier = Modifier.fillMaxHeight(),
                    onClick = {
                        nav.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}