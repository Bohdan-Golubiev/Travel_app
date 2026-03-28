package com.example.travelapp.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(AppDestinations.PROFILE) }

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
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF0D1B2A),
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.CREATE -> CreateScreen()
                    AppDestinations.PROFILE -> ProfileScreen(user = user, onSignOut = onSignOut)
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