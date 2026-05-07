package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.RoutesViewModel

fun Long.toFormattedDate(): String { //з мілісекунд в дату
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}

@Composable
fun RoutesScreen(
    userId: String,
    onOpen: (RouteEntity) -> Unit,
    viewModel: RoutesViewModel = viewModel()
) {
    val routes by viewModel.getRoutes(userId).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(routes, key = { it.id }) { route ->
            RouteItem(
                route = route,
                onClick = { onOpen(route) },
                onDelete = { viewModel.deleteRoute(userId, route.id) }
            )
            HorizontalDivider(color = Color(0xFF2A4A5E))
        }
    }
}

@Composable
private fun RouteItem(
    route: RouteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 10.dp),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                imageVector = if (route.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (route.isFavorite) Color(0xFFFF0000) else Color.Gray
            )
            Text(
                text = route.name,
                fontSize = 15.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = route.createdAt.toFormattedDate(),
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5),
                modifier = Modifier.padding(end = 8.dp)
            )

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFFB0BEC5)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.delete, color = Color(0xFFFF6B6B)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}