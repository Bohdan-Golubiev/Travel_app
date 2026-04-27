package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.viewmodel.profile.PlaceDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: String,
    routeId: String,
    userId: String,
    onAddReview: (PlaceEntity) -> Unit,
    viewModel: PlaceDetailViewModel = viewModel()
) {
    LaunchedEffect(placeId) { viewModel.loadPlace(placeId, routeId) }

    val place by viewModel.place.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val isLoadingReviews by viewModel.isLoadingReviews.collectAsState()

    val avg by viewModel.avgRating.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = place?.name ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = place?.location ?: "",
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5)
                    )
                    if (!place?.visitDate.isNullOrEmpty()) {
                        Text(
                            text = "Visit date: ${place?.visitDate}",
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                    Text(
                        text = "Order in route: ${(place?.orderInRoute?.plus(1)) ?: ""}",
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reviews (${reviews.size})",
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
                        text = "No reviews yet. Be the first!",
                        fontSize = 13.sp,
                        color = Color(0xFF5E7A8A),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                    )
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    ReviewItem(
                        review = review,
                        currentUserId = userId
                    )
                    HorizontalDivider(color = Color(0xFF2A4A5E))
                }
            }
        }

        Button(
            onClick = { place?.let { onAddReview(it) } },
            enabled = place != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
        ) {
            Text("Add review")
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewEntity, currentUserId: String) {
    val formattedDate = remember(review.createdAt) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(review.createdAt))
    }

    val displayName = if (review.userId == currentUserId) {
        "Ви"
    } else {
        review.userName
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${review.mark}/5",
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5)
            )
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
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color(0xFF5E7A8A)
            )
        }
    }
}