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
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.view.ReviewItem
import com.example.travelapp.viewmodel.profile.BookingDetailViewModel

@Composable
fun BookingDetailScreen(
    booking: BookingEntity,
    userId: String,
    onAddReview: (BookingEntity) -> Unit,
) {
    val viewModel: BookingDetailViewModel = viewModel()
    val reviews by viewModel.reviews.collectAsState()
    val isLoadingReviews by viewModel.isLoadingReviews.collectAsState()
    val avg by viewModel.avgRating.collectAsState()

    val targetId = remember(booking.id) { booking.id.removeSuffix(booking.routeId.uppercase()) }
    val strings = LocalAppStrings.current

    LaunchedEffect(targetId) {
        viewModel.loadReviews(targetId)
    }

    val serviceLabel = when (booking.type) {
        "Pl" -> strings.plane
        "Tr" -> "Train"
        "Bs" -> "Bus"
        else -> "Unknown"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A3A4E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${booking.from} → ${booking.to}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = serviceLabel, fontSize = 14.sp, color = Color(0xFFB0BEC5))
                }
            }

            InfoCard(title = strings.departure.uppercase()) {
                InfoRow(label = strings.departure, value = booking.departureTime)
                InfoRow(label = strings.arrival, value = booking.arrivalTime)
            }

            InfoCard(title = strings.cost.uppercase()) {
                InfoRow(label = strings.cost, value = "${booking.cost}  ₴", valueColor = Color(0xFF4FC3F7))
                InfoRow(label = strings.status, value = booking.status)
            }

            InfoCard(title = strings.createdAt.uppercase()) {
                InfoRow(label = strings.createdAt, value = booking.date)
            }

            HorizontalDivider(color = Color(0xFF2A4A5E))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.reviews + " (${reviews.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                if (isLoadingReviews) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF219EBC))
                }
                Text(text = "⭐ ${"%.1f".format(avg)}", color = Color.White)
            }

            HorizontalDivider(color = Color(0xFF2A4A5E))

            if (!isLoadingReviews && reviews.isEmpty()) {
                Text(
                    text = strings.noReviews,
                    fontSize = 13.sp,
                    color = Color(0xFF5E7A8A),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                reviews.forEach { review ->
                    ReviewItem(
                        review = review,
                        currentUserId = userId,
                        youLabel = strings.you
                    )
                }
            }
        }

        Button(
            onClick = { onAddReview(booking) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
        ) {
            Text(strings.addReview)
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
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF607D8B), letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFFB0BEC5), modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Normal)
    }
}