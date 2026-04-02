package com.example.travelapp.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.travelapp.view.create.FindHotelScreen
import com.example.travelapp.view.create.FindVehicleScreen
import com.example.travelapp.view.create.RouteCreatedScreen
import com.example.travelapp.view.create.SearchPlaces


sealed class CreateNavigation(val route: String) {

    data object Create : CreateNavigation("create")

    data object CreateRoute : CreateNavigation("create-route")

    data object SaveRoute : CreateNavigation("save/{routeName}") {
        fun withArgs(routeName: String) = "save/$routeName"
    }

    data object FindVehicle : CreateNavigation("vehicle")

    data object FindHotel : CreateNavigation("hotel")
}


@Composable
fun CreateScreen(nav: NavHostController) {
    NavHost(
        navController = nav,
        startDestination = CreateNavigation.Create.route
    ) {

        composable(CreateNavigation.Create.route) {
            CreateContent(
                onCreate = { nav.navigate(CreateNavigation.CreateRoute.route)
                }
            )
        }

        composable(CreateNavigation.CreateRoute.route) {
            SearchPlaces(
            onSaveRoute = { routeName ->
                nav.navigate(CreateNavigation.SaveRoute.withArgs(routeName))
            }
            )
        }

        composable(
            route = CreateNavigation.SaveRoute.route,
            arguments = listOf(navArgument("routeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeName = backStackEntry.arguments?.getString("routeName") ?: ""

            RouteCreatedScreen(
                routeName = routeName,
                onMakeBooking = {
                    nav.navigate(CreateNavigation.FindVehicle.route)
                }
            )
        }

        composable(CreateNavigation.FindVehicle.route) {
            FindVehicleScreen(
                onNextClick = {
                    nav.navigate(CreateNavigation.FindHotel.route)
                }
            )
        }

        composable(CreateNavigation.FindHotel.route) {
            FindHotelScreen(onNextClick = { })
        }
    }
}
@Composable
fun CreateContent(onCreate: () -> Unit)
{
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Create route",
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }
    }
}