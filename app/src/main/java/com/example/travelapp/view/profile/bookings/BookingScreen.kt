package com.example.travelapp.view.profile.bookings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.BookingViewModel

@Composable
fun BookingScreen(
    userId: String,
    onOpen: (BookingEntity) -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val bookings by viewModel.getBookings(userId).collectAsState(initial = null)
    val strings = LocalAppStrings.current

    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var activeExpanded   by rememberSaveable { mutableStateOf(true) }
    var expiredExpanded  by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            bookings == null -> Unit

            bookings!!.isEmpty() -> {
                EmptyBookingsMessage(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                val active  = bookings!!.filter { !viewModel.isBookingExpired(it.booking.date) }
                val expired = bookings!!.filter {  viewModel.isBookingExpired(it.booking.date) }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (active.isNotEmpty()) {
                        item(key = "header_active") {
                            CollapsibleSectionHeader(
                                title    = strings.bookings + " ✈",
                                expanded = activeExpanded,
                                count    = active.size,
                                onToggle = { activeExpanded = !activeExpanded }
                            )
                        }
                        if (activeExpanded) {
                            items(active, key = { "active_${it.booking.id}" }) { item ->
                                BookingCard(
                                    booking   = item.booking,
                                    routeName = item.routeName,
                                    isExpired = false,
                                    onClick   = { onOpen(item.booking) },
                                    onDelete  = {
                                        viewModel.deleteBooking(
                                            userId,
                                            routeId   = item.booking.routeId,
                                            bookingId = item.booking.id
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (expired.isNotEmpty()) {
                        item(key = "header_expired") {
                            CollapsibleSectionHeader(
                                title    = strings.bookingExpired,
                                expanded = expiredExpanded,
                                count    = expired.size,
                                onToggle = { expiredExpanded = !expiredExpanded }
                            )
                        }
                        if (expiredExpanded) {
                            items(expired, key = { "expired_${it.booking.id}" }) { item ->
                                BookingCard(
                                    booking   = item.booking,
                                    routeName = item.routeName,
                                    isExpired = true,
                                    onClick   = { onOpen(item.booking) },
                                    onDelete  = {
                                        viewModel.deleteBooking(
                                            userId,
                                            routeId   = item.booking.routeId,
                                            bookingId = item.booking.id
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun EmptyBookingsMessage(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    Column(
        modifier            = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            tint               = Color(0xFF219EBC),
            modifier           = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text       = strings.noBookings,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Medium,
            color      = Color(0xFFB0BEC5),
            textAlign  = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = strings.createBookingMes,
            fontSize  = 13.sp,
            color     = Color(0xFF546E7A),
            textAlign = TextAlign.Center
        )
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
        targetValue   = if (expanded) 0f else -90f,
        animationSpec = tween(durationMillis = 200),
        label         = "arrow_rotation"
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
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
private fun BookingCard(
    booking: BookingEntity,
    routeName: String,
    isExpired: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    var menuExpanded     by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text(strings.deleteBookingTitle, fontWeight = FontWeight.Medium) },
            text    = { Text(strings.deleteBookingMessage, color = Color(0xFFB0BEC5)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text(strings.delete, color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel, color = Color(0xFF4FC3F7))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
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
                            text       = booking.name,
                            fontSize   = 14.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            text     = booking.date,
                            fontSize = 12.sp,
                            color    = if (isExpired) Color(0xFF546E7A) else Color(0xFFB0BEC5)
                        )
                    }
                }

                if (isExpired) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3A4E)) {
                        Text(
                            text     = strings.bookingExpired,
                            fontSize = 13.sp,
                            color    = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3D2B)) {
                        Text(
                            text       = "${"%.0f".format(booking.cost)} ₴",
                            fontSize   = 13.sp,
                            color      = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick  = { menuExpanded = true },
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
                        expanded         = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text(strings.delete, color = Color(0xFFFF6B6B)) },
                            onClick = { menuExpanded = false; showDeleteDialog = true }
                        )
                    }
                }
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
                        text       = booking.from,
                        fontSize   = 18.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text     = booking.departureTime,
                        fontSize = 14.sp,
                        color    = Color(0xFF219EBC)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.weight(1f)
                ) {
                    Text(text = "─ ─ ─ ✈ ─ ─ ─", fontSize = 14.sp, color = Color(0xFF2A4A5E))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = booking.to,
                        fontSize   = 18.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text     = booking.arrivalTime,
                        fontSize = 14.sp,
                        color    = Color(0xFF219EBC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E3A50))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = strings.route + routeName,
                fontSize = 12.sp,
                color    = Color(0xFF6B8FA8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}