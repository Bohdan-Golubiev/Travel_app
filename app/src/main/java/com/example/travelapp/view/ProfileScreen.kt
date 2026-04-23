package com.example.travelapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.view.profile.PaymentsScreen
import com.example.travelapp.view.profile.ReviewsScreen
import com.example.travelapp.view.profile.bookings.BookingDetailScreen
import com.example.travelapp.view.profile.bookings.BookingScreen
import com.example.travelapp.view.profile.bookings.HotelDetailScreen
import com.example.travelapp.view.profile.hotels.HotelScreen
import com.example.travelapp.view.profile.routes.AddReviewPlaceScreen
import com.example.travelapp.view.profile.routes.PlaceDetailScreen
import com.example.travelapp.view.profile.routes.RouteDetailScreen
import com.example.travelapp.view.profile.routes.RoutesScreen
import com.example.travelapp.viewmodel.profile.BookingViewModel
import com.google.firebase.auth.FirebaseUser

class SharedViewModel : ViewModel() {
    var selectedPlace: PlaceEntity? = null
    var selectedBooking: BookingEntity? = null
    var selectedHotel: HotelEntity? = null
}
@Composable
fun ProfileScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    nav: NavHostController,
    onTitleChange: (String) -> Unit
) {
    val sharedViewModel: SharedViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    NavHost(
        navController = nav,
        startDestination = ProfileNavigation.Profile.route,
    ) {
        composable(ProfileNavigation.Profile.route) {
            ProfileContent(user = user, onSignOut = onSignOut, nav = nav)
        }

        composable(ProfileNavigation.ListOfRoutes.route) {
            LaunchedEffect(Unit) { onTitleChange("My routes") }
            RoutesScreen(
                userId = user.uid,
                onOpen = { route ->
                    nav.navigate(ProfileNavigation.Route.createRoute(route.id, route.name))
                }
            )
        }
        composable(
            route = ProfileNavigation.Route.route,
            arguments = ProfileNavigation.Route.navArguments
        ) { backStack ->
            val routeId = backStack.arguments?.getString("routeId") ?: ""
            val routeName = backStack.arguments?.getString("routeName") ?: ""
            LaunchedEffect(routeName) { onTitleChange(routeName) }
            RouteDetailScreen(
                routeId = routeId,
                routeName = routeName,
                userId = user.uid,
                onTitleChange = onTitleChange,
                onNext = { place ->
                    nav.navigate(ProfileNavigation.Place.createRoute(routeId, place.id, place.name))
                }
            )
        }

        composable(
            route = ProfileNavigation.Place.route,
            arguments = ProfileNavigation.Place.navArguments
        ) { backStack ->
            val routeId = backStack.arguments?.getString("routeId") ?: ""
            val placeId = backStack.arguments?.getString("placeId") ?: ""
            val placeName = backStack.arguments?.getString("placeName") ?: ""
            LaunchedEffect(placeName) { onTitleChange(placeName) }
            PlaceDetailScreen(
                placeId = placeId,
                routeId = routeId,
                userId = user.uid,
                onAddReview = { place ->
                    sharedViewModel.selectedPlace = place
                    nav.navigate(ProfileNavigation.AddReview.route)
                }
            )
        }

        composable(ProfileNavigation.AddReview.route) {
            val place = sharedViewModel.selectedPlace
            place?.let {
                AddReviewPlaceScreen(
                    place = it,
                    userId = user.uid,
                    onSubmit = { nav.popBackStack() }
                )
            }
        }

        composable(ProfileNavigation.Booking.route) {backStack ->
            LaunchedEffect(Unit) { onTitleChange("My Bookings") }
            BookingScreen(
                userId = user.uid,
                onOpen = { booking ->
                    nav.navigate(
                        ProfileNavigation.BookingDetail.createRoute(
                            booking.id,
                            booking.name
                        )
                    )
                }
            )
        }

        composable(
            route = ProfileNavigation.BookingDetail.route,
            arguments = ProfileNavigation.BookingDetail.navArguments
        ) { backStack ->
            val bookingId = backStack.arguments?.getString("bookingId") ?: ""
            val bookingName = backStack.arguments?.getString("bookingName") ?: ""
            LaunchedEffect(bookingName) { onTitleChange(bookingName) }

            val booking by bookingViewModel.getByBookingId(bookingId)
                .collectAsState(initial = null)

            booking?.let { entity ->
                BookingDetailScreen(booking = entity)
            }
        }

        composable(ProfileNavigation.Hotel.route) {
            LaunchedEffect(Unit) { onTitleChange("My Hotels") }
            HotelScreen(
                userId = user.uid,
                onOpen = { hotel ->
                    nav.navigate(
                        ProfileNavigation.HotelDetail.createRoute(hotel.id, hotel.name)
                    )
                }
            )
        }

        composable(
            route = ProfileNavigation.HotelDetail.route,
            arguments = ProfileNavigation.HotelDetail.navArguments
        ) { backStack ->
            val hotelId = backStack.arguments?.getString("hotelId") ?: ""
            val hotelName = backStack.arguments?.getString("hotelName") ?: ""
            LaunchedEffect(hotelName) { onTitleChange(hotelName) }
            HotelDetailScreen(
                hotelId = hotelId,
                userId = user.uid,
                onDeleted = { nav.popBackStack() }
            )
        }

        composable(ProfileNavigation.Payment.route) {
            LaunchedEffect(Unit) { onTitleChange("My Payments") }
            PaymentsScreen()
        }

        composable(ProfileNavigation.Review.route) {
            LaunchedEffect(Unit) { onTitleChange("My Reviews") }
            ReviewsScreen(userId = user.uid)
        }
    }
}

@Composable
fun ProfileContent(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    nav: NavHostController
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = user.displayName ?: "Name Surname",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Sign out", fontSize = 14.sp)
            }
        }
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileTextField(text = user.email ?: "Email")
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(
            label = "My routes",
            onClick = {
                nav.navigate(ProfileNavigation.ListOfRoutes.route)
            }
        )
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(
            label = "My booking",
            onClick = {
                nav.navigate(ProfileNavigation.Booking.route)
            }
        )
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(
            label = "My hotels",
            onClick = { nav.navigate(ProfileNavigation.Hotel.route) }
        )
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(
            label = "My payments",
            onClick = {
                nav.navigate(ProfileNavigation.Payment.route)
            }
        )
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(
            label = "My reviews",
            onClick = {
                nav.navigate(ProfileNavigation.Review.route)
            }
        )
        HorizontalDivider(color = Color(0xFF2A4A5E))
    }
}
@Composable
private fun ProfileTextField(text: String) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color(0xFFB0BEC5)
        )
    }
}

@Composable
private fun ProfileButton(label: String, onClick: () -> Unit) {

    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.White
        )
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}