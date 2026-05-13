package com.example.travelapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
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
import com.example.travelapp.model.ReviewItem
import com.example.travelapp.model.ReviewTarget
import com.example.travelapp.utils.AppLocale
import com.example.travelapp.utils.LocalAppLocale
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.view.profile.EditReviewScreen
import com.example.travelapp.view.profile.ReviewsScreen
import com.example.travelapp.view.profile.bookings.BookingDetailScreen
import com.example.travelapp.view.profile.bookings.BookingScreen
import com.example.travelapp.view.profile.bookings.HotelDetailScreen
import com.example.travelapp.view.profile.bookings.HotelScreen
import com.example.travelapp.view.profile.routes.AddReviewScreen
import com.example.travelapp.view.profile.routes.PlaceDetailScreen
import com.example.travelapp.view.profile.routes.RouteDetailScreen
import com.example.travelapp.view.profile.routes.RoutesScreen
import com.example.travelapp.viewmodel.profile.BookingViewModel
import com.example.travelapp.viewmodel.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseUser

class SharedViewModel : ViewModel() {
    var selectedPlace: PlaceEntity? = null
    var selectedBooking: BookingEntity? = null
    var selectedHotel: HotelEntity? = null
    var selectedReview: ReviewItem? = null

}
@Composable
fun ProfileScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    nav: NavHostController,
    onTitleChange: (String) -> Unit,
    onLocaleChange: (AppLocale) -> Unit
) {
    val sharedViewModel: SharedViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val strings = LocalAppStrings.current

    NavHost(
        navController = nav,
        startDestination = ProfileNavigation.Profile.route,
    ) {
        composable(ProfileNavigation.Profile.route) {
            ProfileContent(user = user, onSignOut = onSignOut, nav = nav, onLocaleChange )
        }

        composable(ProfileNavigation.ListOfRoutes.route) {
            LaunchedEffect(Unit) { onTitleChange(strings.myRoutes) }
            RoutesScreen(
                userId = user.uid,
                onOpen = { route ->
                    nav.navigate(ProfileNavigation.Route.createRoute(route.id, route.name, route.description))                }
            )
        }
        composable(
            route = ProfileNavigation.Route.route,
            arguments = ProfileNavigation.Route.navArguments
        ) { backStack ->
            val routeId = backStack.arguments?.getString("routeId") ?: ""
            val routeName = backStack.arguments?.getString("routeName") ?: ""
            val routeDescription = backStack.arguments?.getString("routeDescription") ?: ""

            LaunchedEffect(routeName) { onTitleChange(routeName) }
            RouteDetailScreen(
                routeId = routeId,
                routeName = routeName,
                routeDescription = routeDescription,
                userId = user.uid,
                onTitleChange = { newName, newDescription ->
                    onTitleChange(newName)
                    nav.navigate(
                        ProfileNavigation.Route.createRoute(routeId, newName, newDescription)
                    ) {
                        popUpTo(ProfileNavigation.Route.route) { inclusive = true }
                    }
                },
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
            val reviewTarget = sharedViewModel.selectedPlace?.let { ReviewTarget.Place(it) }
                ?: sharedViewModel.selectedHotel?.let { ReviewTarget.Hotel(it)}
                ?: sharedViewModel.selectedBooking?.let { ReviewTarget.Booking(it)}

            reviewTarget?.let { target ->
                AddReviewScreen(
                    target = target,
                    userId = user.uid,
                    onSubmit = { nav.popBackStack() }
                )
            }
        }

        composable(ProfileNavigation.Booking.route) {backStack ->
            LaunchedEffect(Unit) { onTitleChange(strings.myBooking) }
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
                BookingDetailScreen(
                    booking = entity,
                    userId = user.uid,
                    onAddReview = { entity ->
                        sharedViewModel.selectedBooking = entity
                        sharedViewModel.selectedHotel = null
                        sharedViewModel.selectedPlace = null
                        nav.navigate(ProfileNavigation.AddReview.route)
                    })
            }
        }

        composable(ProfileNavigation.Hotel.route) {
            LaunchedEffect(Unit) { onTitleChange(strings.myHotels) }
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
                onAddReview = { hotel ->
                    sharedViewModel.selectedHotel = hotel
                    sharedViewModel.selectedPlace = null
                    nav.navigate(ProfileNavigation.AddReview.route)
                }
            )
        }

        composable(ProfileNavigation.Review.route) {
            LaunchedEffect(Unit) { onTitleChange(strings.myReviews) }
            ReviewsScreen(
                userId = user.uid,
                onEdit = { item ->
                    sharedViewModel.selectedReview = item
                    nav.navigate(ProfileNavigation.EditReview.route)
                }
            )
        }

        composable(ProfileNavigation.EditReview.route) {
            LaunchedEffect(Unit) { onTitleChange(strings.editReview) }
            val item = sharedViewModel.selectedReview
            item?.let {
                EditReviewScreen(
                    review        = it.review,
                    placeName     = when (it) {
                        is ReviewItem.PlaceReview -> it.placeName
                        is ReviewItem.HotelReview -> it.hotelName
                        is ReviewItem.BookingReview -> strings.from + it.from + " ${strings.toLow} " + it.to
                    },
                    placeLocation = when (it) {
                        is ReviewItem.PlaceReview -> it.placeLocation
                        is ReviewItem.HotelReview -> it.hotelAddress
                        is ReviewItem.BookingReview -> strings.flyBy + it.nameBooking + " " + strings.In  + it.date
                    },
                    userId        = user.uid,
                    onSubmit      = { nav.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    nav: NavHostController,
    onLocaleChange: (AppLocale) -> Unit
) {
    val strings       = LocalAppStrings.current
    val profileViewModel: ProfileViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = user.displayName ?: "Name Surname",
                fontSize   = 16.sp,
                color      = Color.White,
                fontWeight = FontWeight.Medium
            )

            Box {
                IconButton(onClick = { profileViewModel.openSettingsMenu() }) {
                    Icon(
                        imageVector        = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint               = Color.White
                    )
                }

                SettingsDropdownMenu(
                    expanded        = profileViewModel.isSettingsMenuOpen,
                    onDismiss       = { profileViewModel.closeSettingsMenu() },
                    onSignOut       = {
                        profileViewModel.closeSettingsMenu()
                        onSignOut()
                    },
                    onLocaleChange  = { locale ->
                        onLocaleChange(locale)
                    }
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileTextField(text = user.email ?: strings.email)
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = strings.myRoutes,  onClick = { nav.navigate(ProfileNavigation.ListOfRoutes.route) })
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = strings.myBooking, onClick = { nav.navigate(ProfileNavigation.Booking.route) })
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = strings.myHotels,  onClick = { nav.navigate(ProfileNavigation.Hotel.route) })
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = strings.myReviews, onClick = { nav.navigate(ProfileNavigation.Review.route) })
        HorizontalDivider(color = Color(0xFF2A4A5E))
    }
}
@Composable
private fun SettingsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit
) {
    val strings       = LocalAppStrings.current
    val currentLocale = LocalAppLocale.current

    DropdownMenu(
        expanded         = expanded,
        onDismissRequest = onDismiss,
        modifier         = Modifier
            .background(Color(0xFF162032))
            .widthIn(min = 220.dp)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text       = strings.selectLanguage,
                    color      = Color(0xFFB0BEC5),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            onClick = {},
            enabled = false
        )

        HorizontalDivider(color = Color(0xFF2A4A5E))

        AppLocale.entries.forEach { locale ->
            val isSelected = locale == currentLocale
            DropdownMenuItem(
                text = {
                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text  = locale.displayName,
                            color = Color.White
                        )
                        if (isSelected) {
                            Icon(
                                imageVector        = Icons.Default.Check,
                                contentDescription = null,
                                tint               = Color(0xFF219EBC),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                onClick = { onLocaleChange(locale) },
                modifier = Modifier.background(
                    if (isSelected) Color(0xFF1A3550) else Color.Transparent
                )
            )
        }

        HorizontalDivider(color = Color(0xFF2A4A5E))

        DropdownMenuItem(
            text = {
                Text(
                    text  = strings.signOut,
                    color = Color(0xFFEF5350)
                )
            },
            onClick = onSignOut
        )
    }
}
@Composable
private fun ProfileTextField(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp)
    ) {
        Text(text = text, fontSize = 15.sp, color = Color(0xFFB0BEC5))
    }
}

@Composable
private fun ProfileButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(0.dp),
        colors   = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text     = label,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}