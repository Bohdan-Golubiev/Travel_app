package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.viewmodel.profile.HotelViewModel

@Composable
fun HotelDetailScreen(
    hotelId: String,
    userId: String,
    onDeleted: () -> Unit
) {
    val viewModel: HotelViewModel = viewModel()
    val hotelWithRoute by viewModel.getHotelWithRouteById(hotelId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }

    hotelWithRoute?.let { data ->
        HotelDetailContent(
            hotelWithRoute = data
        )
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4FC3F7))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete hotel", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete \"${hotelWithRoute?.hotel?.name}\"?",
                    color = Color(0xFFB0BEC5)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        hotelWithRoute?.let { viewModel.deleteHotel(userId, it.hotel) }
                        showDeleteDialog = false
                        onDeleted()
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
}

@Composable
private fun HotelDetailContent(
    hotelWithRoute: HotelWithRoute
) {
    val hotel = hotelWithRoute.hotel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A3A4E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = hotel.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hotel.address,
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }

        InfoCard(title = "Stay period") {
            InfoRow(label = "Check-in", value = hotel.dateFrom)
            InfoRow(label = "Check-out", value = hotel.dateTo)
            InfoRow(label = "Duration", value = "${hotel.days} day(s)")
        }

        InfoCard(title = "Cost") {
            InfoRow(label = "Per day", value = "%.2f".format(hotel.costPerDay))
            InfoRow(
                label = "Total",
                value = "%.2f".format(hotel.totalCost),
                valueColor = Color(0xFF4FC3F7),
            )
        }

        InfoCard(title = "Route") {
            InfoRow(label = "Route name", value = hotelWithRoute.routeName)
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF132D3E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF607D8B),
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.White,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFB0BEC5),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = FontWeight.Normal
        )
    }
}