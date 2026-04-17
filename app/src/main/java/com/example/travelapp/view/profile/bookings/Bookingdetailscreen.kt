package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.data.entity.BookingEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: BookingEntity,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            item {
                BookingDetailRow(
                    leftText = "Direction",
                    rightText = "${booking.from} → ${booking.to}"
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                BookingDetailRow(
                    leftText = "Service",
                    rightText = when (booking.type) {
                        "Pl" -> "Plane"
                        "Tr" -> "Train"
                        "Bs" -> "Bus"
                        else -> "Unknown"
                    }
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                BookingDetailRow(
                    leftText = "Cost",
                    rightText = "${booking.cost} $"
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                BookingDetailRow(
                    leftText = "Status",
                    rightText = booking.status
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                BookingDetailRow(
                    leftText = "Created at",
                    rightText = booking.date
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                BookingDetailRow(
                    leftText = "Departure",
                    rightText = booking.departureTime
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                BookingDetailRow(
                    leftText = "Arrival",
                    rightText = booking.arrivalTime
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }

        Button(
            onClick = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC)),
        ) {
            Text("Add review")
        }
    }
}

@Composable
private fun BookingDetailRow(leftText: String, rightText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = leftText, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
        Text(text = rightText, fontSize = 15.sp, color = Color(0xFFB0BEC5))
    }
}