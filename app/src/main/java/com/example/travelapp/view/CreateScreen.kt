package com.example.travelapp.view

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.travelapp.model.ReviewTarget
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.view.create.BookingOption
import com.example.travelapp.view.create.FindHotelScreen
import com.example.travelapp.view.create.FindVehicleScreen
import com.example.travelapp.view.create.HotelBookedScreen
import com.example.travelapp.view.create.RouteCreatedScreen
import com.example.travelapp.view.create.SearchPlaces
import com.example.travelapp.view.profile.routes.AddReviewScreen
import com.example.travelapp.view.profile.routes.PlaceDetailScreen
import com.example.travelapp.view.profile.routes.RouteDetailScreen
import com.example.travelapp.view.profile.routes.RoutesScreen
import com.example.travelapp.viewmodel.create.SelectedHotelEntry
import com.google.firebase.auth.FirebaseUser

sealed class CreateNavigation(val route: String) {

    data object ListOfRoutes : CreateNavigation("list")

    data object Route : CreateNavigation("route/{routeId}/{routeName}/{routeDescription}") {
        fun createRoute(routeId: String, routeName: String, routeDescription: String) =
            "route/$routeId/${Uri.encode(routeName)}/${Uri.encode(routeDescription)}"

        val navArguments = listOf(
            navArgument("routeId") { type = NavType.StringType },
            navArgument("routeName") { type = NavType.StringType },
            navArgument("routeDescription") { type = NavType.StringType }
        )
    }

    data object Place : CreateNavigation("place/{routeId}/{placeId}/{placeName}") {
        fun createRoute(routeId: String, placeId: String, placeName: String) =
            "place/$routeId/$placeId/$placeName"

        val navArguments = listOf(
            navArgument("routeId") { type = NavType.StringType },
            navArgument("placeId") { type = NavType.StringType },
            navArgument("placeName") { type = NavType.StringType }
        )
    }
    data object AddReview : CreateNavigation("add_review")

    data object CreateRoute : CreateNavigation("create-route")

    data object SaveRoute : CreateNavigation("save/{routeName}") {
        fun withArgs(routeName: String) = "save/$routeName"
    }

    data object FindVehicle : CreateNavigation("vehicle/{routeId}") {
        fun withArgs(routeId: String) = "vehicle/$routeId"
    }

    data object FindHotel : CreateNavigation("hotel/{routeId}") {
        fun withArgs(routeId: String) = "hotel/$routeId"
    }
    data object HotelBooked : CreateNavigation("hotel-booked")
}

@Composable
fun CreateScreen(
    currentUser: FirebaseUser,
    nav: NavHostController,
    onTitleChange: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    val savedRouteId = remember { mutableStateOf("") }
    val sharedViewModel: SharedViewModel = viewModel()

    val bookedVehicles = rememberSaveable { mutableStateOf<List<BookingOption>>(emptyList()) }
    val bookedHotels = rememberSaveable { mutableStateOf<List<SelectedHotelEntry>>(emptyList()) }

    NavHost(
        navController    = nav,
        startDestination = CreateNavigation.ListOfRoutes.route
    ) {

        composable(CreateNavigation.ListOfRoutes.route) {
            LaunchedEffect(Unit) { onTitleChange(strings.myRoutes) }
            RoutesScreen(
                userId = currentUser.uid,
                onOpen = { route ->
                    nav.navigate(
                        CreateNavigation.Route.createRoute(route.id, route.name, route.description)
                    )
                },
                onCreateRoute = {
                    nav.navigate(CreateNavigation.CreateRoute.route)
                }
            )
        }

        composable(
            route = CreateNavigation.Route.route,
            arguments = CreateNavigation.Route.navArguments
        ) { backStack ->
            val routeId          = backStack.arguments?.getString("routeId") ?: ""
            val routeName        = backStack.arguments?.getString("routeName") ?: ""
            val routeDescription = backStack.arguments?.getString("routeDescription") ?: ""

            LaunchedEffect(routeName) { onTitleChange(routeName) }
            RouteDetailScreen(
                routeId          = routeId,
                routeName        = routeName,
                routeDescription = routeDescription,
                userId           = currentUser.uid,
                onTitleChange    = { newName, newDescription ->
                    onTitleChange(newName)
                    nav.navigate(
                        CreateNavigation.Route.createRoute(routeId, newName, newDescription)
                    ) {
                        popUpTo(CreateNavigation.Route.route) { inclusive = true }
                    }
                },
                onNext = { place ->
                    nav.navigate(CreateNavigation.Place.createRoute(routeId, place.id, place.name))
                }
            )
        }

        composable(
            route = CreateNavigation.Place.route,
            arguments = CreateNavigation.Place.navArguments
        ) { backStack ->
            val routeId   = backStack.arguments?.getString("routeId") ?: ""
            val placeId   = backStack.arguments?.getString("placeId") ?: ""
            val placeName = backStack.arguments?.getString("placeName") ?: ""
            LaunchedEffect(placeName) { onTitleChange(placeName) }
            PlaceDetailScreen(
                placeId     = placeId,
                routeId     = routeId,
                userId      = currentUser.uid,
                onAddReview = { place ->
                    sharedViewModel.setReviewTarget(ReviewTarget.Place(place))
                    nav.navigate(CreateNavigation.AddReview.route)
                }
            )
        }

        composable(CreateNavigation.AddReview.route) {
            val reviewTarget = sharedViewModel.selectedPlace?.let  { ReviewTarget.Place(it) }
                ?: sharedViewModel.selectedHotel?.let              { ReviewTarget.Hotel(it) }
                ?: sharedViewModel.selectedBooking?.let            { ReviewTarget.Booking(it) }

            LaunchedEffect(reviewTarget?.name ?: "") { onTitleChange(reviewTarget?.name ?: "") }
            reviewTarget?.let { target ->
                AddReviewScreen(
                    target   = target,
                    userId   = currentUser.uid,
                    onSubmit = { nav.popBackStack() }
                )
            }
        }

        composable(CreateNavigation.CreateRoute.route) {
            SearchPlaces(
                userId = currentUser.uid,
                onSaveRoute = { routeName, routeId ->
                    savedRouteId.value = routeId
                    nav.navigate(CreateNavigation.SaveRoute.withArgs(routeName))
                }
            )
        }

        composable(
            route     = CreateNavigation.SaveRoute.route,
            arguments = listOf(navArgument("routeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeName = backStackEntry.arguments?.getString("routeName") ?: ""

            RouteCreatedScreen(
                routeName    = routeName,
                onMakeBooking = {
                    nav.navigate(CreateNavigation.FindVehicle.withArgs(savedRouteId.value))
                }
            )
        }

        composable(
            route     = CreateNavigation.FindVehicle.route,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""

            FindVehicleScreen(
                userId      = currentUser.uid,
                routeId     = routeId,
                onNextClick = { selected ->
                    bookedVehicles.value = selected
                    nav.navigate(CreateNavigation.FindHotel.withArgs(savedRouteId.value))
                }
            )
        }

        composable(
            route     = CreateNavigation.FindHotel.route,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: savedRouteId.value

            FindHotelScreen(
                userId      = currentUser.uid,
                routeId     = routeId,
                onNextClick = { selected ->
                    bookedHotels.value = selected
                    nav.navigate(CreateNavigation.HotelBooked.route)
                }
            )
        }

        composable(CreateNavigation.HotelBooked.route) {
            HotelBookedScreen(
                selectedHotels   = bookedHotels.value,
                selectedVehicles = bookedVehicles.value,
                onDoneClick      = {
                    nav.navigate(CreateNavigation.ListOfRoutes.route) {
                        popUpTo(CreateNavigation.ListOfRoutes.route) { inclusive = true }
                    }
                }
            )
        }
    }
}