package com.example.travelapp.view

import androidx.compose.foundation.BorderStroke
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

fun NavGraphBuilder.profileGraph(
    nav: NavHostController,
    user: FirebaseUser,
    sharedViewModel: SharedViewModel,
    onSignOut: () -> Unit,
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

        reviewTarget?.let { target ->
            AddReviewScreen(
                target = target,
                userId = user.uid,
                onSubmit = { nav.popBackStack() }
            )
        }
    }

    composable(ProfileNavigation.Booking.route) {
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
        val bookingViewModel: BookingViewModel = viewModel()

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
        sharedViewModel.selectedReview?.let { item ->
            val review = item.review
            EditReviewScreen(
                review        = review,
                placeName     = when (item) {
                    is ReviewItem.PlaceReview   -> review.targetName
                    is ReviewItem.HotelReview   -> review.targetName
                    is ReviewItem.BookingReview -> strings.from + review.from + " ${strings.toLow} " + review.to
                },
                placeLocation = when (item) {
                    is ReviewItem.PlaceReview   -> review.location
                    is ReviewItem.HotelReview   -> review.location
                    is ReviewItem.BookingReview -> strings.flyBy + review.targetName + " " + strings.In + review.date
                },
                userId   = user.uid,
                onSubmit = { nav.popBackStack() }
            )
        }
    }

    composable(ProfileNavigation.ActiveTrips.route) {
        ActiveTripsScreen(userId = user.uid)
    }

    composable(ProfileNavigation.SpendingStats.route) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = user.displayName ?: "Name Surname",
                            fontSize   = 20.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text      = user.email ?: strings.email,
                            fontSize  = 13.sp,
                            color     = Color(0xFF7EA8BE),
                            letterSpacing = 0.2.sp
                        )
                    }

                    Box {
                        IconButton(
                            onClick  = { profileViewModel.openSettingsMenu() },
                            modifier = Modifier
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint               = Color(0xFF219EBC),
                                modifier           = Modifier.size(30.dp)
                            )
                        }

                        SettingsDropdownMenu(
                            expanded         = profileViewModel.isSettingsMenuOpen,
                            onDismiss        = { profileViewModel.closeSettingsMenu() },
                            onSignOut        = {
                                profileViewModel.closeSettingsMenu()
                                onSignOut()
                            },
                            onLocaleChange   = onLocaleChange,
                            profileViewModel = profileViewModel,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ProfileButton(
                label   = strings.activeTrips,
                icon    = "✈",
                onClick = { nav.navigate(ProfileNavigation.ActiveTrips.route) }
            )
            ProfileButton(
                label   = strings.statsSpending,
                icon    = "📊",
                onClick = { nav.navigate(ProfileNavigation.SpendingStats.route) }
            )
            ProfileButton(
                label   = strings.myBooking,
                icon    = "🎫",
                onClick = { nav.navigate(ProfileNavigation.Booking.route) }
            )
            ProfileButton(
                label   = strings.myHotels,
                icon    = "🏨",
                onClick = { nav.navigate(ProfileNavigation.Hotel.route) }
            )
            ProfileButton(
                label   = strings.myReviews,
                icon    = "⭐",
                onClick = { nav.navigate(ProfileNavigation.Review.route) }
            )
        }
    }
}

@Composable
private fun SettingsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
    profileViewModel: ProfileViewModel
) {
    val strings       = LocalAppStrings.current
    val currentLocale = LocalAppLocale.current

    DropdownMenu(
        expanded         = expanded,
        onDismissRequest = onDismiss,
        modifier         = Modifier
            .background(Color(0xFF162032))
            .widthIn(min = 220.dp),
        border = BorderStroke(1.dp, Color(0xFF7EA8BE))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.notifications,
                        color = Color.White
                    )
                    Switch(
                        checked = profileViewModel.notificationsEnabled,
                        onCheckedChange = { profileViewModel.toggleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF219EBC),
                            checkedTrackColor = Color(0xFF1A3550),
                            uncheckedThumbColor = Color(0xFFB0BEC5),
                            uncheckedTrackColor = Color(0xFF2A4A5E)
                        )
                    )
                }
            },
            onClick = {
                profileViewModel.toggleNotifications(
                    !profileViewModel.notificationsEnabled
                )
            }
        )

        HorizontalDivider(color = Color(0xFF2A4A5E))

        DropdownMenuItem(
            text = { Text(text = strings.signOut, color = Color(0xFFEF5350)) },
            onClick = onSignOut
        )
    }
}
@Composable
private fun ProfileButton(label: String, icon: String, onClick: () -> Unit) {
    Surface(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(14.dp),
        color     = Color(0xFF162032),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFF1A3550),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 16.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text       = label,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFFDCECF4),
                    letterSpacing = 0.2.sp
                )
            }
            Text(
                text     = "›",
                fontSize = 30.sp,
                color    = Color(0xFF4A7A96),
                fontWeight = FontWeight.Light
            )
        }
    }
}