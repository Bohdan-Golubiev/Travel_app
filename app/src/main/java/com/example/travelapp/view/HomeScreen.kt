package com.example.travelapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.utils.AppLocale
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.google.firebase.auth.FirebaseUser

object RootRoutes {
    const val CREATE = "root_create"
    const val PROFILE = "root_profile"
}

private val TAB_ROOT_ROUTES = setOf(
    CreateNavigation.ListOfRoutes.route,
    ProfileNavigation.Profile.route,
)

private val CREATE_TOPBAR_ROUTES = setOf(
    CreateNavigation.ListOfRoutes.route,
    CreateNavigation.Route.route,
    CreateNavigation.Place.route,
    CreateNavigation.AddReview.route,
    CreateNavigation.FindVehicle.route,
    CreateNavigation.FindHotel.route,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit
) {
    val nav = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    var selectedDestination by rememberSaveable {
        mutableStateOf(AppDestinations.CREATE)
    }

    selectedDestination = when {
        currentDestination.isInHierarchy(RootRoutes.PROFILE) ->
            AppDestinations.PROFILE

        currentDestination.isInHierarchy(RootRoutes.CREATE) ->
            AppDestinations.CREATE

        else -> selectedDestination
    }

    val showTopBar = when (selectedDestination) {
        AppDestinations.PROFILE ->
            currentDestination?.route != ProfileNavigation.Profile.route

        AppDestinations.CREATE ->
            currentDestination?.route in CREATE_TOPBAR_ROUTES
    }

    fun AppDestinations.label(strings: AppStrings) = when (this) {
        AppDestinations.CREATE -> strings.trips
        AppDestinations.PROFILE -> strings.profile
    }

    val canGoBack = currentDestination?.route != null &&
            currentDestination.route !in TAB_ROOT_ROUTES

    val strings = LocalAppStrings.current

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(destination.icon, null)
                    },
                    label = {
                        Text(destination.label(strings))
                    },
                    selected = selectedDestination == destination,
                    onClick = {
                        selectedDestination = destination

                        val rootRoute = when (destination) {
                            AppDestinations.CREATE -> RootRoutes.CREATE
                            AppDestinations.PROFILE -> RootRoutes.PROFILE
                        }

                        nav.navigate(rootRoute) {
                            popUpTo(nav.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ){
        Scaffold(
            containerColor = Color(0xFF0D1B2A),
            topBar = {
                if (showTopBar) {
                    AppTopBar(
                        route = backStackEntry?.destination?.route,
                        args = backStackEntry?.arguments,
                        nav = nav,
                        canGoBack = canGoBack,
                        strings = strings
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D1B2A),
                                Color(0xFF346585)
                            )
                        )
                    )
            ) {
                AppNavHost(
                    nav = nav,
                    user = user,
                    sharedViewModel = sharedViewModel,
                    onSignOut = onSignOut,
                    onLocaleChange = onLocaleChange,
                    onTitleChange = { title -> title
                    }
                )
            }
        }
    }
}

private fun NavDestination?.isInHierarchy(route: String): Boolean {

    return this?.hierarchy?.any {
        it.route == route
    } == true
}

@Composable
private fun AppNavHost(
    nav: NavHostController,
    user: FirebaseUser,
    sharedViewModel: SharedViewModel,
    onSignOut: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
    onTitleChange: (String) -> Unit,
) {
    NavHost(
        navController = nav,
        startDestination = RootRoutes.CREATE
    ) {
        navigation(
            route = RootRoutes.CREATE,
            startDestination = CreateNavigation.ListOfRoutes.route
        ) {
            createGraph(
                nav = nav,
                currentUser = user,
                sharedViewModel = sharedViewModel,
                onTitleChange = onTitleChange
            )
        }

        navigation(
            route = RootRoutes.PROFILE,
            startDestination = ProfileNavigation.Profile.route
        ) {
            profileGraph(
                nav = nav,
                user = user,
                sharedViewModel = sharedViewModel,
                onSignOut = onSignOut,
                onLocaleChange = onLocaleChange
            )
        }
    }
}

enum class AppDestinations(val icon: ImageVector) {
    CREATE(Icons.Default.Create),
    PROFILE(Icons.Default.Person),
}