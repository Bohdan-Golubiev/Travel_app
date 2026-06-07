package com.example.travelapp.view.profile.routes

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.setValue
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
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.data.repository.WeatherInfo
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.create.PlaceItem
import com.example.travelapp.viewmodel.create.routes.PlaceInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

private val SheetBackground = Color(0xFF0D2233)
private val SurfaceCard = Color(0xFF152D40)
private val AccentBlue = Color(0xFF219EBC)
private val TextPrimary = Color(0xFFECF0F1)
private val TextSecondary = Color(0xFF8FAAB8)
private val DividerColor = Color(0xFF1E3A50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoBottomSheet(
    place: PlaceItem,
    currentUserId: String,
    onDismiss: () -> Unit,
    viewModel: PlaceInfoViewModel = viewModel()
) {
    val weather         by viewModel.weather.collectAsState()
    val isLoadingWeather by viewModel.isLoadingWeather.collectAsState()
    val reviews         by viewModel.reviews.collectAsState()
    val isLoadingReviews by viewModel.isLoadingReviews.collectAsState()
    val avgRating       by viewModel.avgRating.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isLoadingPhotos by viewModel.isLoadingPhotos.collectAsState()

    LaunchedEffect(place.id) {
        viewModel.load(place)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(TextSecondary.copy(alpha = 0.4f))
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = place.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = place.location,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    if (place.visitDate.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "📅 ${place.visitDate}",
                            fontSize = 13.sp,
                            color = AccentBlue
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
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

            if (place.visitDate.isNotBlank() && isWithinTenDays(place.visitDate)) {
                item {
                    WeatherBlock(
                        isLoading = isLoadingWeather,
                        weather = weather,
                        visitDate = place.visitDate
                    )
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = DividerColor)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val strings = LocalAppStrings.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.reviews,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        if (reviews.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = AccentBlue.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "${reviews.size}",
                                    fontSize = 12.sp,
                                    color = AccentBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (isLoadingReviews) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = AccentBlue
                            )
                        }
                    }

                    if (reviews.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⭐", fontSize = 14.sp)
                            Text(
                                text = "%.1f".format(avgRating),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "/ 5",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            if (!isLoadingReviews) {
                if (reviews.isEmpty()) {
                    item { NoReviewsCard() }
                } else {
                    items(reviews.take(3), key = { it.id }) { review -> // залочено на 3 відгуки щоб не перекривати повністю екран
                        ReviewCard(review = review, currentUserId = currentUserId)
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = AccentBlue
                        )
                    }
                }
            }
        }
    }
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
private fun WeatherBlock(
    isLoading: Boolean,
    weather: WeatherInfo?,
    visitDate: String
) {
    val strings = LocalAppStrings.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = strings.weatherOn + visitDate,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AccentBlue
                )
                Text(
                    text = strings.loadWeather,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        AnimatedVisibility(visible = !isLoading && weather != null, enter = fadeIn(), exit = fadeOut()) {
            weather?.let { w ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = w.conditionIconUrl,
                            contentDescription = w.conditionText,
                            modifier = Modifier.size(44.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${"%.0f".format(w.tempC)}°C  •  ${w.conditionText}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("💧 ${w.humidity}%",    fontSize = 12.sp, color = TextSecondary)
                                Text("💨 ${"%.0f".format(w.windKph)} " + strings.speed, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = !isLoading && weather == null, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = strings.weatherUnavailable,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun NoReviewsCard() {
    val strings = LocalAppStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🗺️", fontSize = 28.sp)
            Text(
                text = strings.noMark,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewEntity, currentUserId: String) {
    val strings = LocalAppStrings.current
    val formattedDate = remember(review.createdAt) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(review.createdAt))
    }
    val displayName = if (review.userId == currentUserId) strings.you else review.userName

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { i ->
                        Text(
                            text = if (i < review.mark) "⭐" else "☆",
                            fontSize = 11.sp,
                            color = if (i < review.mark) Color(0xFFFFC107) else TextSecondary
                        )
                    }
                }
            }

            if (review.text.isNotBlank()) {
                Text(
                    text = review.text,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun isWithinTenDays(visitDate: String): Boolean {
    val sdf  = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val visit = runCatching { sdf.parse(visitDate) }.getOrNull() ?: return false
    val diffDays = java.util.concurrent.TimeUnit.MILLISECONDS
        .toDays(visit.time - Date().time)
    return diffDays in 0..10
}