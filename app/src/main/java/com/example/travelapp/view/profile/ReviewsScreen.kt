package com.example.travelapp.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            uiState.placeReviews.isEmpty() && uiState.hotelReviews.isEmpty() -> {
                Text(
                    text = "Відгуків ще немає",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (uiState.placeReviews.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Місця")
                        }
                        items(
                            items = uiState.placeReviews,
                            key = { it.review.id }
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

                    if (uiState.hotelReviews.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Готелі")
                        }
                        items(
                            items = uiState.hotelReviews,
                            key = { it.review.id }
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

                    if (uiState.bookingReviews.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Рейси")
                        }
                        items(
                            items = uiState.bookingReviews,
                            key = { it.review.id }
                        ) { item ->
                            ReviewCard(
                                title    = "From " + item.fromTo,
                                subtitle = "Fly by " + item.nameBooking + "\n" +
                                        "in " + item.date,
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

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF219EBC),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
    HorizontalDivider(color = Color(0xFF2A4A5E))
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

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Видалити відгук?") },
            text = { Text("Цю дію неможливо скасувати.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onDelete()
                }) { Text("Видалити", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Скасувати") }
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
                    text = "Оцінка: ${review.mark}/5",
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
                            text = { Text("Видалити", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showConfirmDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Редагувати", color = MaterialTheme.colorScheme.secondary) },
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