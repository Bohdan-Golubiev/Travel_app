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
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.viewmodel.profile.HotelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HotelDetailScreen(
    hotelId: String,
    userId: String,
    onAddReview: (HotelEntity) -> Unit,
) {
    val viewModel: HotelViewModel = viewModel()
    val hotelWithRoute by viewModel.getHotelWithRouteById(hotelId).collectAsState(initial = null)

    hotelWithRoute?.let { data ->
        HotelDetailContent(
            hotelWithRoute = data,
            userId = userId,
            onAddReview = { onAddReview(data.hotel) },
            viewModel = viewModel
        )
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4FC3F7))
        }
    }
}

@Composable
private fun HotelDetailContent(
    hotelWithRoute: HotelWithRoute,
    onAddReview: (HotelEntity) -> Unit,
    userId: String,
    viewModel: HotelViewModel
) {
    val hotel = hotelWithRoute.hotel
    val reviews by viewModel.reviews.collectAsState()
    val isLoadingReviews by viewModel.isLoadingReviews.collectAsState()
    val hotelKey = remember(hotel.id) { hotel.id.removeSuffix(hotel.routeId) }
    val avg by viewModel.avgRating.collectAsState()

    LaunchedEffect(hotelKey) {
        viewModel.loadReviews(hotelKey)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp), // bottom для кнопки
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
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

        HorizontalDivider(color = Color(0xFF2A4A5E))

        Row(
            modifier = Modifier.fillMaxWidth(),
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

        if (!isLoadingReviews && reviews.isEmpty()) {
            Text(
                text = "No reviews yet. Be the first!",
                fontSize = 13.sp,
                color = Color(0xFF5E7A8A),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            reviews.forEach { review ->
                ReviewItem(review = review, currentUserId = userId)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { onAddReview(hotel) },
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
            .fillMaxWidth(),
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
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color(0xFF5E7A8A)
            )
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