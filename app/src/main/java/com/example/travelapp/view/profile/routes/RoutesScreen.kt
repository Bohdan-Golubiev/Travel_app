package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.model.dataclasses.Route

val sampleRoutes = listOf(
    Route(1, "Route name 1", "created at"),
    Route(2, "Route name 2", "created at"),
    Route(3, "Route name 3", "created at"),
    Route(4, "Route name 4", "created at"),
)

@Composable
fun RoutesScreen(onOpen: (Route) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(sampleRoutes, key = { it.id }) { route ->
            RouteItem(route = route, onClick = { onOpen(route) })
            HorizontalDivider(color = Color(0xFF2A4A5E))
        }
    }
}

@Composable
private fun RouteItem(route: Route, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
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
            Text(text = route.name, fontSize = 15.sp, color = Color.White)
            Text(text = route.createdAt, fontSize = 13.sp, color = Color(0xFFB0BEC5))
        }
    }
}