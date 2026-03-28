package com.example.travelapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(user: FirebaseUser, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Верхній рядок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.displayName ?: "Name Surname",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Sign out", fontSize = 14.sp)
            }
        }

        HorizontalDivider(color = Color(0xFF2A4A5E))

        // Email
        ProfileTextField(text = user.email ?: "Email")

        HorizontalDivider(color = Color(0xFF2A4A5E))

        // Кнопки навігації
        ProfileButton(label = "My routes")
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = "My booking")
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = "My payments")
        HorizontalDivider(color = Color(0xFF2A4A5E))

        ProfileButton(label = "My reviews")
        HorizontalDivider(color = Color(0xFF2A4A5E))
    }
}
sealed class RoutesRoute(val route: String) {
    data object Profile : RoutesRoute("profile")
    data object ListOfRoutes : RoutesRoute("list")
    data object Route: RoutesRoute("route")
    data object Place : RoutesRoute("place")
}
@Composable // текстове поле
private fun ProfileTextField(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color(0xFFB0BEC5)
        )
    }
}

@Composable // поле-кнопка
private fun ProfileButton(label: String) {
    TextButton(
        onClick = {  },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}