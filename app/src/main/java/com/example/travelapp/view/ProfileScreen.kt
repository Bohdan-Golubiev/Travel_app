package com.example.travelapp.view

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.model.ReviewItem
import com.example.travelapp.model.ReviewTarget
import com.example.travelapp.utils.AppLocale
import com.example.travelapp.utils.LocalAppLocale
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.view.create.BookingOption
import com.example.travelapp.view.profile.ActiveTripsScreen
import com.example.travelapp.view.profile.EditReviewScreen
import com.example.travelapp.view.profile.ReviewsScreen
import com.example.travelapp.view.profile.SpendingStatsScreen
import com.example.travelapp.view.profile.bookings.BookingDetailScreen
import com.example.travelapp.view.profile.bookings.BookingScreen
import com.example.travelapp.view.profile.bookings.HotelDetailScreen
import com.example.travelapp.view.profile.bookings.HotelScreen
import com.example.travelapp.view.profile.routes.AddReviewScreen
import com.example.travelapp.viewmodel.create.SelectedHotelEntry
import com.example.travelapp.viewmodel.profile.BookingViewModel
import com.example.travelapp.viewmodel.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseUser

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

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.profileGraph(
    nav: NavHostController,
    user: FirebaseUser,
    sharedViewModel: SharedViewModel,
    onSignOut: () -> Unit,
    onTitleChange: (String) -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
) {
    composable(ProfileNavigation.Profile.route) {
        ProfileContent(
            user           = user,
            onSignOut      = onSignOut,
            nav            = nav,
            onLocaleChange = onLocaleChange
        )
    }

    composable(ProfileNavigation.AddReview.route) {
        val reviewTarget = sharedViewModel.selectedPlace?.let { ReviewTarget.Place(it) }
            ?: sharedViewModel.selectedHotel?.let { ReviewTarget.Hotel(it) }
            ?: sharedViewModel.selectedBooking?.let { ReviewTarget.Booking(it) }

        LaunchedEffect(reviewTarget?.name ?: "") { onTitleChange(reviewTarget?.name ?: "") }
        reviewTarget?.let { target ->
            AddReviewScreen(
                target = target,
                userId = user.uid,
                onSubmit = { nav.popBackStack() }
            )
        }
    }

    composable(ProfileNavigation.Booking.route) {
        val strings = LocalAppStrings.current
        LaunchedEffect(Unit) { onTitleChange(strings.myBooking) }
        BookingScreen(
            userId = user.uid,
            onOpen = { booking ->
                nav.navigate(
                    ProfileNavigation.BookingDetail.createRoute(booking.id, booking.name)
                )
            }
        )
    }

    composable(
        route     = ProfileNavigation.BookingDetail.route,
        arguments = ProfileNavigation.BookingDetail.navArguments
    ) { backStack ->
        val bookingId   = backStack.arguments?.getString("bookingId") ?: ""
        val bookingName = backStack.arguments?.getString("bookingName") ?: ""
        val bookingViewModel: BookingViewModel = viewModel()

        LaunchedEffect(bookingName) { onTitleChange(bookingName) }

        val booking by bookingViewModel.getByBookingId(bookingId)
            .collectAsState(initial = null)

        booking?.let { entity ->
            BookingDetailScreen(
                booking     = entity,
                userId      = user.uid,
                onAddReview = { e ->
                    sharedViewModel.setReviewTarget(ReviewTarget.Booking(e))
                    nav.navigate(ProfileNavigation.AddReview.route)
                }
            )
        }
    }

    composable(ProfileNavigation.Hotel.route) {
        val strings = LocalAppStrings.current
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
        route     = ProfileNavigation.HotelDetail.route,
        arguments = ProfileNavigation.HotelDetail.navArguments
    ) { backStack ->
        val hotelId   = backStack.arguments?.getString("hotelId") ?: ""
        val hotelName = backStack.arguments?.getString("hotelName") ?: ""

        LaunchedEffect(hotelName) { onTitleChange(hotelName) }
        HotelDetailScreen(
            hotelId     = hotelId,
            userId      = user.uid,
            onAddReview = { hotel ->
                sharedViewModel.setReviewTarget(ReviewTarget.Hotel(hotel))
                nav.navigate(ProfileNavigation.AddReview.route)
            }
        )
    }

    composable(ProfileNavigation.Review.route) {
        val strings = LocalAppStrings.current
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
        val strings = LocalAppStrings.current
        LaunchedEffect(Unit) { onTitleChange(strings.editReview) }
        sharedViewModel.selectedReview?.let { item ->
            EditReviewScreen(
                review        = item.review,
                placeName     = when (item) {
                    is ReviewItem.PlaceReview   -> item.placeName
                    is ReviewItem.HotelReview   -> item.hotelName
                    is ReviewItem.BookingReview -> strings.from + item.from + " ${strings.toLow} " + item.to
                },
                placeLocation = when (item) {
                    is ReviewItem.PlaceReview   -> item.placeLocation
                    is ReviewItem.HotelReview   -> item.hotelAddress
                    is ReviewItem.BookingReview -> strings.flyBy + item.nameBooking + " " + strings.In + item.date
                },
                userId   = user.uid,
                onSubmit = { nav.popBackStack() }
            )
        }
    }

    composable(ProfileNavigation.ActiveTrips.route) {
        val strings = LocalAppStrings.current
        LaunchedEffect(Unit) { onTitleChange(strings.activeTrips) }
        ActiveTripsScreen(userId = user.uid)
    }

    composable(ProfileNavigation.SpendingStats.route) {
        val strings = LocalAppStrings.current
        LaunchedEffect(Unit) { onTitleChange(strings.statsSpending) }
        SpendingStatsScreen(userId = user.uid)
    }
}

@Composable
fun ProfileContent(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    nav: NavHostController,
    onLocaleChange: (AppLocale) -> Unit
) {
    val strings = LocalAppStrings.current
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
                    onLocaleChange = onLocaleChange
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileTextField(text = user.email ?: strings.email)
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(
            label   = strings.activeTrips,
            onClick = { nav.navigate(ProfileNavigation.ActiveTrips.route) }
        )
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = strings.statsSpending, onClick = { nav.navigate(ProfileNavigation.SpendingStats.route) })
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
            text = { Text(text = strings.signOut, color = Color(0xFFEF5350)) },
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