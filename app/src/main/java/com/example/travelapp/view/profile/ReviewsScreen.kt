package com.example.travelapp.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.ui.theme.TextPrimary
import com.example.travelapp.ui.theme.TextSecondary
import com.example.travelapp.viewmodel.profile.review.ReviewsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReviewWithPlace(
    val review: ReviewEntity,
    val placeName: String? = null,
    val placeLocation: String? = null
)
@Composable
fun ReviewsScreen(
    userId: String,
    onEdit: (ReviewWithPlace) -> Unit,
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.reviews.isEmpty() -> {
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
                    items(
                        items = uiState.reviews,
                        key = { it.review.id }
                    ) { review ->
                        ReviewCard(
                            item = review,
                            onDelete = { viewModel.deleteReview(userId, review.review) },
                            onEdit = { onEdit(review) }
                        )
                        HorizontalDivider(color = Color(0xFF2A4A5E))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    item: ReviewWithPlace,
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
                }) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Скасувати")
                }
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
                text = item.placeName.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Оцінка: ${item.review.mark}/5",
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
                            text = {
                                Text(
                                    "Видалити",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showConfirmDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Редагувати",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            },
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
            Text(
                text = item.placeLocation.toString(),
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = item.review.createdAt.toFormattedDate(),
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.review.text,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}
private fun Long.toFormattedDate(): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(this))