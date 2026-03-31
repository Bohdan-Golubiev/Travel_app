package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Тестові дані ─────────────────────────────────────────────────────────────

data class Review(
    val id: Int,
    val user: String,
    val rating: String,
    val text: String,
    val createdAt: String
)

val sampleReviews = listOf(
    Review(1, "User", "Rating n/N", "Review text", "created at"),
    Review(2, "User", "Rating n/N", "Review text", "created at"),
    Review(3, "User", "Rating n/N", "Review text", "created at"),
)

// ─── Екран ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: Int,
    onAddReview: () -> Unit = {},
) {
    val place = samplePlaces.find { it.id == placeId }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // щоб список не ховався під кнопкою
        ) {

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = place?.name ?: "Place name",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = place?.location ?: "Info",
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            items(sampleReviews, key = { it.id }) { review ->
                ReviewItem(review = review)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }

        Button(
            onClick = onAddReview,
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
private fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // User + Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = review.user, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(text = review.rating, fontSize = 13.sp, color = Color(0xFFB0BEC5))
        }
        // Review text + created at
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = review.text, fontSize = 13.sp, color = Color(0xFFB0BEC5))
            Text(text = review.createdAt, fontSize = 12.sp, color = Color(0xFF5E7A8A))
        }
    }
}