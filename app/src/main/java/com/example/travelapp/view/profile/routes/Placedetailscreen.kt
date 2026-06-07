package com.example.travelapp.view.profile.routes

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.WeatherInfo
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.create.routes.PlaceDetailViewModel
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
    val weather by viewModel.weather.collectAsState()
    val isLoadingWeather by viewModel.isLoadingWeather.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isLoadingPhotos by viewModel.isLoadingPhotos.collectAsState()

    val avg by viewModel.avgRating.collectAsState()
    val strings = LocalAppStrings.current

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
                            text = strings.visitDate + place?.visitDate,
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                    Text(
                        text = (strings.orderInRoute + (place?.orderInRoute?.plus(1))),
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            if (isLoadingPhotos || photos.isNotEmpty()) {
                item {
                    PlacePhotosSection(
                        photos = photos,
                        isLoading = isLoadingPhotos
                    )
                    HorizontalDivider(color = Color(0xFF2A4A5E))
                }
            }

            val visitDate = place?.visitDate
            if (!visitDate.isNullOrBlank() && isWithinTenDays(visitDate)) {
                item {
                    WeatherSection(
                        isLoading = isLoadingWeather,
                        weather = weather,
                        visitDate = visitDate
                    )
                    HorizontalDivider(color = Color(0xFF2A4A5E))
                }
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
                        color = Color.White
                    )
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }

            if (!isLoadingReviews && reviews.isEmpty()) {
                item {
                    Text(
                        text = strings.noReviews,
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
            Text(strings.addReview)
        }
    }
}

private fun isWithinTenDays(visitDate: String): Boolean {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val visit = runCatching { sdf.parse(visitDate) }.getOrNull() ?: return false
    val diffDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(visit.time - Date().time)
    return diffDays in 0..10
}

@Composable
private fun PlacePhotosSection(
    photos: List<Bitmap>,
    isLoading: Boolean
) {
    var selectedPhoto by remember { mutableStateOf<Bitmap?>(null) }

    selectedPhoto?.let { bitmap ->
        FullScreenPhotoDialog(
            bitmap = bitmap,
            onDismiss = { selectedPhoto = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Фото локації",
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF219EBC)
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos) { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 260.dp, height = 160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedPhoto = bitmap },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenPhotoDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* не закривати при кліку на фото */ },
                contentScale = ContentScale.FillWidth
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрити",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
@Composable
private fun WeatherSection(
    isLoading: Boolean,
    weather: WeatherInfo?,
    visitDate: String
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = strings.weatherOn + visitDate,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        when {
            isLoading -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF219EBC)
                    )
                }
            }

            weather != null -> {
                WeatherCard(weather)
            }

            else -> {}
        }
    }
}

@Composable
private fun WeatherCard(weather: WeatherInfo) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = weather.conditionIconUrl,
            contentDescription = weather.conditionText,
            modifier = Modifier.size(48.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = strings.avgTemp + " •  ${"%.0f".format(weather.tempC)}°C",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "💧 ${weather.humidity}%",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "💨 ${"%.0f".format(weather.windKph)} км/г",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewEntity, currentUserId: String) {
    val formattedDate = remember(review.createdAt) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(review.createdAt))
    }
    val strings = LocalAppStrings.current

    val displayName = if (review.userId == currentUserId) {
        strings.you
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