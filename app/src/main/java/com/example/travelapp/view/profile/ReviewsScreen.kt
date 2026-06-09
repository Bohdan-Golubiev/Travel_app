package com.example.travelapp.view.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.ReviewEntity
import com.example.travelapp.model.ReviewItem
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                                title    = strings.locations + "📍",
                                expanded = placesExpanded,
                                count    = uiState.placeReviews.size,
                                onToggle = { placesExpanded = !placesExpanded }
                            )
                        }
                        item(key = "anim_places") {
                            AnimatedVisibility(
                                visible = placesExpanded,
                                enter   = expandVertically(animationSpec = tween(durationMillis = 280)),
                                exit    = shrinkVertically(animationSpec = tween(durationMillis = 220))
                            ) {
                                Column {
                                    uiState.placeReviews.forEach { item ->
                                        PlaceReviewCard(
                                            review   = item.review,
                                            onDelete = { viewModel.deleteReview(userId, item.review) },
                                            onEdit   = { onEdit(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.hotelReviews.isNotEmpty()) {
                        item(key = "header_hotels") {
                            CollapsibleSectionHeader(
                                title    = strings.hotels + "🏨",
                                expanded = hotelsExpanded,
                                count    = uiState.hotelReviews.size,
                                onToggle = { hotelsExpanded = !hotelsExpanded }
                            )
                        }
                        item(key = "anim_hotels") {
                            AnimatedVisibility(
                                visible = hotelsExpanded,
                                enter   = expandVertically(animationSpec = tween(durationMillis = 280)),
                                exit    = shrinkVertically(animationSpec = tween(durationMillis = 220))
                            ) {
                                Column {
                                    uiState.hotelReviews.forEach { item ->
                                        HotelReviewCard(
                                            review   = item.review,
                                            onDelete = { viewModel.deleteReview(userId, item.review) },
                                            onEdit   = { onEdit(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.bookingReviews.isNotEmpty()) {
                        item(key = "header_bookings") {
                            CollapsibleSectionHeader(
                                title    = strings.flights + " ✈ ",
                                expanded = bookingsExpanded,
                                count    = uiState.bookingReviews.size,
                                onToggle = { bookingsExpanded = !bookingsExpanded }
                            )
                        }
                        item(key = "anim_bookings") {
                            AnimatedVisibility(
                                visible = bookingsExpanded,
                                enter   = expandVertically(animationSpec = tween(durationMillis = 280)),
                                exit    = shrinkVertically(animationSpec = tween(durationMillis = 220))
                            ) {
                                Column {
                                    uiState.bookingReviews.forEach { item ->
                                        FlightReviewCard(
                                            review   = item.review,
                                            onDelete = { viewModel.deleteReview(userId, item.review) },
                                            onEdit   = { onEdit(item) }
                                        )
                                    }
                                }
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
private fun ReviewActionsMenu(
    mark: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1A3D2B)
        ) {
            Text(
                text = "⭐ $mark/5",
                fontSize = 13.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.MoreVert,
                    contentDescription = "Дії",
                    tint               = Color(0xFFB0BEC5),
                    modifier           = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded          = menuExpanded,
                onDismissRequest  = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text    = { Text(strings.edit, color = MaterialTheme.colorScheme.secondary) },
                    onClick = { menuExpanded = false; onEdit() }
                )
                DropdownMenuItem(
                    text    = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                    onClick = { menuExpanded = false; showConfirmDialog = true }
                )
            }
        }
    }
}
@Composable
private fun PlaceReviewCard(
    review: ReviewEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112233)),
        border = BorderStroke(1.dp, Color(0xFF1E3A50))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B3A4B)
                    ) {
                        Text(
                            text     = "📍",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = review.targetName,
                            fontSize   = 14.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            text     = review.location,
                            fontSize = 12.sp,
                            color    = Color(0xFF219EBC)
                        )
                    }
                }
                ReviewActionsMenu(
                    mark     = review.mark,
                    onEdit   = onEdit,
                    onDelete = onDelete
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E3A50))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = review.text,
                fontSize   = 13.sp,
                color      = Color(0xFFB0BEC5),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date
            Text(
                text     = review.createdAt.toFormattedDate(),
                fontSize = 11.sp,
                color    = Color(0xFF6B8FA8)
            )
        }
    }
}
@Composable
private fun HotelReviewCard(
    review: ReviewEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112233)),
        border = BorderStroke(1.dp, Color(0xFF1E3A50))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B3A4B)
                    ) {
                        Text(
                            text     = "🏨",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = review.targetName,
                            fontSize   = 14.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            text     = review.location,
                            fontSize = 12.sp,
                            color    = Color(0xFF219EBC)
                        )
                    }
                }
                ReviewActionsMenu(
                    mark     = review.mark,
                    onEdit   = onEdit,
                    onDelete = onDelete
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E3A50))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = review.text,
                fontSize   = 13.sp,
                color      = Color(0xFFB0BEC5),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = review.createdAt.toFormattedDate(),
                fontSize = 11.sp,
                color    = Color(0xFF6B8FA8)
            )
        }
    }
}
@Composable
private fun FlightReviewCard(
    review: ReviewEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112233)),
        border = BorderStroke(1.dp, Color(0xFF1E3A50))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B3A4B)
                    ) {
                        Text(
                            text     = "✈",
                            fontSize = 18.sp,
                            color    = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = review.targetName,
                            fontSize   = 14.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            text     = review.date,
                            fontSize = 12.sp,
                            color    = Color(0xFFB0BEC5)
                        )
                    }
                }
                ReviewActionsMenu(
                    mark     = review.mark,
                    onEdit   = onEdit,
                    onDelete = onDelete
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E3A50))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = review.from,
                        fontSize   = 16.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.weight(1f)
                ) {
                    Text(
                        text     = "─ ─ ─ ✈ ─ ─ ─",
                        fontSize = 14.sp,
                        color    = Color(0xFF2A4A5E)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = review.to,
                        fontSize   = 16.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E3A50))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = review.text,
                fontSize   = 13.sp,
                color      = Color(0xFFB0BEC5),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = review.createdAt.toFormattedDate(),
                fontSize = 11.sp,
                color    = Color(0xFF6B8FA8)
            )
        }
    }
}

private fun Long.toFormattedDate(): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(this))