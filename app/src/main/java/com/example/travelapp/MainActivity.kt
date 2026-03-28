package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.view.AuthScreen
import com.example.travelapp.view.HomeScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelAppTheme {
                TravelAuthApp()
            }
        }
    }
}

@Composable
fun TravelAuthApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    if (currentUser != null) {
        HomeScreen(
            user = currentUser!!,
            onSignOut = {
                auth.signOut()
                currentUser = null
            }
        )
    } else {
        AuthScreen(onAuthSuccess = { user -> currentUser = user })
    }
}