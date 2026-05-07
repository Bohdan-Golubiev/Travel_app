package com.example.travelapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.utils.AppLocale
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit
) {
    val mainNav = rememberNavController()
    val createNav = rememberNavController()

    var currentDestinationName by rememberSaveable {
        mutableStateOf(AppDestinations.PROFILE.name)
    }
    val currentDestination =
        AppDestinations.valueOf(currentDestinationName)


    val backStackEntry by mainNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showTopBar = currentDestination == AppDestinations.PROFILE
            && currentRoute != ProfileNavigation.Profile.route

    var topBarTitle by remember { mutableStateOf("") }

    val canGoBack =
        when (currentDestination) {
            AppDestinations.PROFILE -> mainNav.previousBackStackEntry != null
            AppDestinations.CREATE -> createNav.previousBackStackEntry != null
        }

    val strings = LocalAppStrings.current

    fun AppDestinations.label(strings: AppStrings) = when (this) {
        AppDestinations.CREATE -> strings.create
        AppDestinations.PROFILE -> strings.profile
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label(strings)
                        )
                    },
                    label = { Text(destination.label(strings)) },
                    selected = currentDestination == destination,
                    onClick = { currentDestinationName = destination.name }
                )
            }
        }
    ){
        Scaffold(
            containerColor = Color(0xFF0D1B2A),
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        modifier = Modifier.height(64.dp),
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF0D1B2A)
                        ),
                        title = {
                            Row(
                                modifier = Modifier.fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = topBarTitle, color = Color.White)
                            }
                        },
                        navigationIcon = {
                            if (canGoBack) {
                                IconButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    onClick = { mainNav.popBackStack() }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
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
                        colors = listOf(Color(0xFF0D1B2A), Color(0xFF25485E))
                    ))
            ) {
                when (currentDestination) {
                    AppDestinations.CREATE -> CreateScreen(
                        currentUser = user,
                        nav = createNav
                    )

                    AppDestinations.PROFILE -> ProfileScreen(
                        user = user,
                        onSignOut = onSignOut,
                        nav = mainNav,
                        onTitleChange = { topBarTitle = it },
                        onLocaleChange = onLocaleChange
                    )
                }
            }
        }
    }
}
enum class AppDestinations(val icon: ImageVector) {
    CREATE(Icons.Default.Create),
    PROFILE(Icons.Default.Person),
}