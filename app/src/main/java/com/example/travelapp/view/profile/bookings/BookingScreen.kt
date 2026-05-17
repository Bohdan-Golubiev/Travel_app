package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.BookingEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    userId: String,
    onOpen: (BookingEntity) -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val bookings by viewModel.getBookings(userId).collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            bookings == null -> Unit

            bookings!!.isEmpty() -> {
                EmptyBookingsMessage(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(bookings!!, key = { it.booking.id }) { item ->
                        BookingItem(
                            booking = item.booking,
                            routeName = item.routeName,
                            onClick = { onOpen(item.booking) },
                            onDelete = {
                                viewModel.deleteBooking(
                                    userId,
                                    routeId = item.booking.routeId,
                                    bookingId = item.booking.id
                                )
                            },
                            viewModel
                        )
                        HorizontalDivider(color = Color(0xFF2A4A5E))
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
private fun BookingItem(
    booking: BookingEntity,
    routeName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    viewModel: BookingViewModel
) {
    val strings = LocalAppStrings.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isExpired = remember(booking.date) { viewModel.isBookingExpired(booking.date) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = strings.deleteBookingTitle,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Text(
                    text = strings.deleteBookingMessage,
                    color = Color(0xFFB0BEC5)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B))
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = booking.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    if (isExpired) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1A3A4E)
                        ) {
                            Text(
                                text = strings.bookingExpired,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1A3A4E)
                        ) {
                            Text(
                                text = "${booking.cost} ₴",
                                fontSize = 13.sp,
                                color = Color(0xFF4FC3F7),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.route + routeName,
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )
                    Text(
                        text = booking.date,
                        fontSize = 13.sp,
                        color = if (isExpired) Color(0xFF546E7A) else Color(0xFFB0BEC5)
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFFB0BEC5)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.delete, color = Color(0xFFFF6B6B)) },
                        onClick = {
                            menuExpanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}