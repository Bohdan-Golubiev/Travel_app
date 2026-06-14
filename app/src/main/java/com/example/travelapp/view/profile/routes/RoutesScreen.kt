package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.RouteEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.create.routes.RoutesViewModel

fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}

@Composable
fun RoutesScreen(
    userId: String,
    onOpen: (RouteEntity) -> Unit,
    onCreateRoute: (() -> Unit)? = null,
    viewModel: RoutesViewModel = viewModel()
) {
    val routes by viewModel.getRoutes(userId).collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            routes == null -> Unit

            routes!!.isEmpty() -> {
                EmptyRoutesMessage(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(routes!!, key = { it.id }) { route ->
                        RouteItem(
                            route             = route,
                            onClick           = { onOpen(route) },
                            onDelete          = { viewModel.deleteRoute(userId, route.id) },
                            onToggleCompleted = { isCompleted ->
                                viewModel.setRouteCompleted(route.id, isCompleted)
                            }
                        )
                    }
                }
            }
        }

        if (onCreateRoute != null) {
            FloatingActionButton(
                onClick = onCreateRoute,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF219EBC)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Створити маршрут")
            }
        }
    }
}

@Composable
private fun EmptyRoutesMessage(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current

    Column(
        modifier            = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint               = Color(0xFF219EBC),
            modifier           = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text       = strings.noRoutes,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Medium,
            color      = Color(0xFFB0BEC5),
            textAlign  = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = strings.createRouteMes,
            fontSize  = 13.sp,
            color     = Color(0xFF546E7A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RouteItem(
    route: RouteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text(strings.deleteRoute) },
            text    = { Text("${strings.route}\"${route.name}\"${strings.deleteRouteMes}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B))
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel, color = Color(0xFF219EBC))
                }
            },
            containerColor = Color(0xFF1B3A4B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0BEC5)
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (route.isCompleted) Color(0xFF0F1F2E) else Color(0xFF112233)
        ),
        border = BorderStroke(
            1.dp,
            if (route.isCompleted) Color(0xFF1A2D3D) else Color(0xFF1E3A50)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (route.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (route.isFavorite) Color(0xFFFF5252) else Color(0xFF4A6A80),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = if (route.isCompleted) {
                        LocalTextStyle.current.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = Color(0xFF4A6A80)
                        )
                    } else {
                        LocalTextStyle.current.copy(color = Color.White)
                    }
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = strings.createdAt + " " + route.createdAt.toFormattedDate(),
                    fontSize = 13.sp,
                    color = Color(0xFF52738D)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (route.isCompleted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A3D2B)
                ) {
                    Text(
                        text = strings.complete,
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint               = Color(0xFF4A6A80),
                        modifier           = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded         = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text(strings.delete, color = Color(0xFFFF6B6B)) },
                        onClick = {
                            menuExpanded    = false
                            showDeleteDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text  = if (route.isCompleted) strings.makeUnComplete else strings.makeComplete,
                                color = Color(0xFF4CAF50)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleCompleted(!route.isCompleted)
                        }
                    )
                }
            }
        }
    }
}