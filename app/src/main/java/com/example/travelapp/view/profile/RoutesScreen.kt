package com.example.travelapp.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Тестові дані ─────────────────────────────────────────────────────────────

data class Route(
    val id: Int,
    val name: String,
    val createdAt: String
)

val sampleRoutes = listOf(
    Route(1, "Route name", "created at"),
    Route(2, "Route name", "created at"),
    Route(3, "Route name", "created at"),
    Route(4, "Route name", "created at"),
)

// ─── Екран ────────────────────────────────────────────────────────────────────

@Composable
fun RoutesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // ─── Header з кнопкою назад ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "My routes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        HorizontalDivider(color = Color(0xFF2A4A5E))

        // ─── Список маршрутів ─────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(sampleRoutes, key = { it.id }) { route ->
                RouteItem(
                    route = route,
                    onClick = { }
                    //onClick = { onRouteClick(route) }
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }
    }
}

// ─── Елемент списку ───────────────────────────────────────────────────────────

@Composable
private fun RouteItem(
    route: Route,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = route.name,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                text = route.createdAt,
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5)
            )
        }
    }
}