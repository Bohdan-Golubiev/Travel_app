package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.BookingDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                BookingDetailRow(leftText = strings.direction, rightText = "${booking.from} → ${booking.to}")
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
            item {
                BookingDetailRow(
                    leftText = strings.service,
                    rightText = when (booking.type) {
                        "Pl" -> strings.plane
                        "Tr" -> "Train"
                        "Bs" -> "Bus"
                        else -> "Unknown"
                    }
                )
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
            item {
                BookingDetailRow(leftText = strings.cost, rightText = "${booking.cost} $")
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
            item {
                BookingDetailRow(leftText = strings.status, rightText = booking.status)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
            item {
                BookingDetailRow(leftText = strings.createdAt, rightText = booking.date)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
            item {
                BookingDetailRow(leftText = strings.departure, rightText = booking.departureTime)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
            item {
                BookingDetailRow(leftText = strings.arrival, rightText = booking.arrivalTime)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                HorizontalDivider(color = Color(0xFF2A4A5E))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.reviews + "( " + reviews.size + " )",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    if (isLoadingReviews) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF219EBC)
                        )
                    }
                    Text(
                        text = "⭐ ${"%.1f".format(avg)}",
                        color = Color.White)
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            if (!isLoadingReviews && reviews.isEmpty()) {
                item {
                    Text(
                        text = strings.noReviews,
                        fontSize = 13.sp,
                        color = Color(0xFF5E7A8A),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }
            } else {
                items(reviews.size) { index ->
                    BookingReviewItem(review = reviews[index], currentUserId = userId)
                    HorizontalDivider(color = Color(0xFF2A4A5E))
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC)),
        ) {
            Text(strings.addReview)
        }
    }
}

@Composable
private fun BookingReviewItem(review: ReviewEntity, currentUserId: String) {
    val formattedDate = remember(review.createdAt) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(review.createdAt))
    }
    val strings = LocalAppStrings.current


    val displayName = if (review.userId == currentUserId) strings.you else review.userName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = displayName, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(text = "${review.mark}/5", fontSize = 13.sp, color = Color(0xFFB0BEC5))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = review.text,
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5),
                modifier = Modifier.weight(1f)
            )
            Text(text = formattedDate, fontSize = 12.sp, color = Color(0xFF5E7A8A))
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