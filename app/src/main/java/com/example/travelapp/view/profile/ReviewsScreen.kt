package com.example.travelapp.view.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.model.ReviewItem
import com.example.travelapp.ui.theme.TextPrimary
import com.example.travelapp.ui.theme.TextSecondary
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.review.ReviewsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReviewsScreen(
    userId: String,
    onEdit: (ReviewItem) -> Unit,
    viewModel: ReviewsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    var placesExpanded by rememberSaveable { mutableStateOf(true) }
    var hotelsExpanded by rememberSaveable { mutableStateOf(true) }
    var bookingsExpanded by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(userId) {
        viewModel.loadReviews(userId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.placeReviews.isEmpty() &&
                    uiState.hotelReviews.isEmpty() &&
                    uiState.bookingReviews.isEmpty() -> {
                Text(
                    text = strings.noReviewsScreen,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (uiState.placeReviews.isNotEmpty()) {
                        item(key = "header_places") {
                            CollapsibleSectionHeader(
                                title    = strings.locations,
                                expanded = placesExpanded,
                                count    = uiState.placeReviews.size,
                                onToggle = { placesExpanded = !placesExpanded }
                            )
                        }
                        if (placesExpanded) {
                            items(
                                items = uiState.placeReviews,
                                key   = { "place_${it.review.id}" }
                            ) { item ->
                                ReviewCard(
                                    title    = item.placeName,
                                    subtitle = item.placeLocation,
                                    review   = item.review,
                                    onDelete = { viewModel.deleteReview(userId, item.review) },
                                    onEdit   = { onEdit(item) }
                                )
                                HorizontalDivider(color = Color(0xFF2A4A5E))
                            }
                        }
                    }

                    if (uiState.hotelReviews.isNotEmpty()) {
                        item(key = "header_hotels") {
                            CollapsibleSectionHeader(
                                title    = strings.hotels,
                                expanded = hotelsExpanded,
                                count    = uiState.hotelReviews.size,
                                onToggle = { hotelsExpanded = !hotelsExpanded }
                            )
                        }
                        if (hotelsExpanded) {
                            items(
                                items = uiState.hotelReviews,
                                key   = { "hotel_${it.review.id}" }
                            ) { item ->
                                ReviewCard(
                                    title    = item.hotelName,
                                    subtitle = item.hotelAddress,
                                    review   = item.review,
                                    onDelete = { viewModel.deleteReview(userId, item.review) },
                                    onEdit   = { onEdit(item) }
                                )
                                HorizontalDivider(color = Color(0xFF2A4A5E))
                            }
                        }
                    }

                    if (uiState.bookingReviews.isNotEmpty()) {
                        item(key = "header_bookings") {
                            CollapsibleSectionHeader(
                                title    = strings.flights,
                                expanded = bookingsExpanded,
                                count    = uiState.bookingReviews.size,
                                onToggle = { bookingsExpanded = !bookingsExpanded }
                            )
                        }
                        if (bookingsExpanded) {
                            items(
                                items = uiState.bookingReviews,
                                key   = { "booking_${it.review.id}" }
                            ) { item ->
                                ReviewCard(
                                    title    = strings.from + item.from + " ${strings.toLow} " + item.to,
                                    subtitle = strings.flyBy + item.nameBooking + "\n" +
                                            strings.In + item.date,
                                    review   = item.review,
                                    onDelete = { viewModel.deleteReview(userId, item.review) },
                                    onEdit   = { onEdit(item) }
                                )
                                HorizontalDivider(color = Color(0xFF2A4A5E))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    count: Int,
    onToggle: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        animationSpec = tween(durationMillis = 200),
        label = "arrow_rotation"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF219EBC)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text     = "($count)",
                    fontSize = 12.sp,
                    color    = Color(0xFF219EBC).copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector        = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Згорнути" else "Розгорнути",
                tint               = Color(0xFF219EBC),
                modifier           = Modifier
                    .size(20.dp)
                    .rotate(arrowRotation)
            )
        }
        HorizontalDivider(color = Color(0xFF2A4A5E))
    }
}

@Composable
private fun ReviewCard(
    title: String,
    subtitle: String,
    review: ReviewEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(strings.deleteReview) },
            text = { Text(strings.alertReview) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onDelete()
                }) { Text(strings.delete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = strings.mark + "${review.mark}/5",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Дії",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showConfirmDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.edit, color = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
            Text(
                text = review.createdAt.toFormattedDate(),
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = review.text,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

private fun Long.toFormattedDate(): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(this))