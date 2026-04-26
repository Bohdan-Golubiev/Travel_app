package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.viewmodel.profile.HotelViewModel

@Composable
fun HotelScreen(
    userId: String,
    onOpen: (HotelEntity) -> Unit
) {
    val viewModel: HotelViewModel = viewModel()
    val hotels by viewModel.getHotelsByUser(userId).collectAsState(initial = emptyList())

    if (hotels.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hotels yet",
                color = Color(0xFFB0BEC5),
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(hotels, key = { it.hotel.id }) { hotelWithRoute ->
                HotelListItem(
                    hotelWithRoute = hotelWithRoute,
                    onClick = { onOpen(hotelWithRoute.hotel) },
                    onDelete = { viewModel.deleteHotel(userId, hotelWithRoute.hotel)}
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }
    }
}

@Composable
private fun HotelListItem(
    hotelWithRoute: HotelWithRoute,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val hotel = hotelWithRoute.hotel
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete hotel", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete \"${hotelWithRoute.hotel.name}\" from \"${hotelWithRoute.routeName}\" route?",
                    color = Color(0xFFB0BEC5)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFF4FC3F7))
                }
            },
            containerColor = Color(0xFF0D2535),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = hotel.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${hotel.dateFrom}  →  ${hotel.dateTo}",
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = hotelWithRoute.routeName,
                fontSize = 12.sp,
                color = Color(0xFF607D8B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1A3A4E)
        ) {
            Text(
                text = "${"%.0f".format(hotel.totalCost)} ₴",
                fontSize = 13.sp,
                color = Color(0xFF4FC3F7),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
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
                    text = { Text("Delete", color = Color(0xFFFF6B6B)) },
                    onClick = {
                        menuExpanded = false
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}