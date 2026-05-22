package com.example.travelapp.view.profile.bookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.dao.HotelWithRoute
import com.example.travelapp.data.entity.HotelEntity
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.HotelStatus
import com.example.travelapp.viewmodel.profile.HotelViewModel

@Composable
fun HotelScreen(
    userId: String,
    onOpen: (HotelEntity) -> Unit,
    viewModel: HotelViewModel = viewModel()
) {
    val hotels by viewModel.getHotelsByUser(userId).collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            hotels == null -> Unit

            hotels!!.isEmpty() -> {
                EmptyHotelsMessage(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(hotels!!, key = { it.hotel.id }) { hotelWithRoute ->
                        HotelListItem(
                            hotelWithRoute = hotelWithRoute,
                            onClick = { onOpen(hotelWithRoute.hotel) },
                            onDelete = { viewModel.deleteHotel(userId, hotelWithRoute.hotel) },
                            viewModel = viewModel
                        )
                        HorizontalDivider(color = Color(0xFF2A4A5E))
                    }
                }
            }
        }
    }
}
@Composable
private fun EmptyHotelsMessage(modifier: Modifier = Modifier) {
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
            text       = strings.noHotelsScreen,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Medium,
            color      = Color(0xFFB0BEC5),
            textAlign  = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = strings.createHotelMes,
            fontSize  = 13.sp,
            color     = Color(0xFF546E7A),
            textAlign = TextAlign.Center
        )
    }
}
@Composable
private fun HotelListItem(
    hotelWithRoute: HotelWithRoute,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    viewModel: HotelViewModel,
) {
    val hotel = hotelWithRoute.hotel
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    val status = remember(hotel.dateFrom, hotel.dateTo) {
        viewModel.getHotelStatus(hotel.dateFrom, hotel.dateTo)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteHotel, fontWeight = FontWeight.Medium) },
            text = {
                Text(strings.alertHotel + hotel.name + strings.fromHotel + hotelWithRoute.routeName + strings.routeLow)
            },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hotel.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${hotel.dateFrom}  →  ${hotel.dateTo}",
                fontSize = 13.sp,
                color = if (status == HotelStatus.COMPLETED) Color(0xFF546E7A) else Color(0xFFB0BEC5)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.route + hotelWithRoute.routeName,
                fontSize = 12.sp,
                color = Color(0xFF607D8B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        when (status) {
            HotelStatus.UPCOMING -> {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3A4E)) {
                    Text(
                        text = "${"%.0f".format(hotel.totalCost)} ₴",
                        fontSize = 13.sp,
                        color = Color(0xFF4FC3F7),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HotelStatus.IN_PROGRESS -> {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3A4E)) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${"%.0f".format(hotel.totalCost)} ₴",
                            fontSize = 13.sp,
                            color = Color(0xFF4FC3F7),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = strings.inProgress,
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HotelStatus.COMPLETED -> {
                Surface(shape = RoundedCornerShape(8.dp),
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