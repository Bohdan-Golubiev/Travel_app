package com.example.travelapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: FirebaseUser, onSignOut: () -> Unit) {
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

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
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
                        onTitleChange = { topBarTitle = it }
                    )
                }
            }
        }
    }
}
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    CREATE("Create", Icons.Default.Create),
    PROFILE("Profile", Icons.Default.Person),
}