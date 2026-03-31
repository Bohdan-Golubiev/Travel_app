package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.model.dataclasses.Booking


val sampleBookings = listOf(
    Booking(1, "Booking 1", "route", "status (unpay)", "created at", "service", 500, "information about service with large text"),
    Booking(2, "Booking 2", "route", "status (pay)", "created at", "service", 1000, "information about service with large text\ninformation about service with large text"),
    Booking(3, "Booking 3", "route", "status (unpay)", "created at", "service", 2000, "information about service with large text"),
    Booking(4, "Booking 4", "route", "status (pay)", "created at", "service", 3000, "information about service with large text\ninformation about service with large text"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onOpen: (Booking) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(sampleBookings, key = { it.id }) { booking ->
            BookingItem(booking = booking, onClick = { onOpen(booking) })
            HorizontalDivider(color = Color(0xFF2A4A5E))
        }
    }
}

@Composable
private fun BookingItem(booking: Booking, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = booking.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(text = booking.status, fontSize = 13.sp, color = Color(0xFFB0BEC5))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = booking.route, fontSize = 13.sp, color = Color(0xFFB0BEC5))
                Text(text = booking.createdAt, fontSize = 13.sp, color = Color(0xFFB0BEC5))
            }
        }
    }
}