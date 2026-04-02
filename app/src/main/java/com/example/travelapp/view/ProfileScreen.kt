package com.example.travelapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.travelapp.view.profile.PaymentsScreen
import com.example.travelapp.view.profile.ReviewsScreen
import com.example.travelapp.view.profile.bookings.BookingDetailScreen
import com.example.travelapp.view.profile.bookings.BookingScreen
import com.example.travelapp.view.profile.routes.PlaceDetailScreen
import com.example.travelapp.view.profile.routes.RouteDetailScreen
import com.example.travelapp.view.profile.routes.RoutesScreen
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    nav: NavHostController,
    onTitleChange: (String) -> Unit
) {
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
                onOpen = { route ->
                    nav.navigate(ProfileNavigation.Route.createRoute(route.id, route.name))
                }
            )
        }
            composable(
                route = ProfileNavigation.Route.route,
                arguments = ProfileNavigation.Route.navArguments
            ) { backStack ->
                val routeName = backStack.arguments?.getString("routeName") ?: ""
                LaunchedEffect(routeName) { onTitleChange(routeName) }
                RouteDetailScreen(
                    onNext = { place ->
                        nav.navigate(ProfileNavigation.Place.createRoute(place.id, place.name))
                    }
                )
            }

            composable(
                route = ProfileNavigation.Place.route,
                arguments = ProfileNavigation.Place.navArguments
            ) { backStack ->
                val placeId = backStack.arguments?.getInt("placeId") ?: 0
                val placeName = backStack.arguments?.getString("placeName") ?: ""
                LaunchedEffect(placeName) { onTitleChange(placeName) }
                PlaceDetailScreen(
                    placeId = placeId
                )
            }

            composable(ProfileNavigation.Booking.route) {
                LaunchedEffect(Unit) { onTitleChange("My Bookings") }
                BookingScreen(
                    onOpen = { booking ->
                        nav.navigate(ProfileNavigation.BookingDetail.createRoute(booking.id, booking.name))
                    }
                )
            }

            composable(
                route = ProfileNavigation.BookingDetail.route,
                arguments = ProfileNavigation.BookingDetail.navArguments
            ) { backStack ->
                val bookingId = backStack.arguments?.getInt("bookingId") ?: 0
                val bookingName = backStack.arguments?.getString("bookingName") ?: ""
                LaunchedEffect(bookingName) { onTitleChange(bookingName) }
                BookingDetailScreen(
                    bookingId = bookingId,
                )
            }
            composable(ProfileNavigation.Payment.route) {
                LaunchedEffect(Unit) { onTitleChange("My Payments") }
                PaymentsScreen()
            }

            composable(ProfileNavigation.Review.route) {
                LaunchedEffect(Unit) { onTitleChange("My Reviews") }
                ReviewsScreen()
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