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
    val strings = LocalAppStrings.current

    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var upcomingExpanded   by rememberSaveable { mutableStateOf(true) }
    var inProgressExpanded by rememberSaveable { mutableStateOf(true) }
    var completedExpanded  by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            hotels == null -> Unit

            hotels!!.isEmpty() -> {
                EmptyHotelsMessage(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                val upcoming   = hotels!!.filter {
                    viewModel.getHotelStatus(it.hotel.dateFrom, it.hotel.dateTo) == HotelStatus.UPCOMING
                }
                val inProgress = hotels!!.filter {
                    viewModel.getHotelStatus(it.hotel.dateFrom, it.hotel.dateTo) == HotelStatus.IN_PROGRESS
                }
                val completed  = hotels!!.filter {
                    viewModel.getHotelStatus(it.hotel.dateFrom, it.hotel.dateTo) == HotelStatus.COMPLETED
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (inProgress.isNotEmpty()) {
                        item(key = "header_inprogress") {
                            CollapsibleSectionHeader(
                                title    = strings.inProgress + " 🏨",
                                expanded = inProgressExpanded,
                                count    = inProgress.size,
                                onToggle = { inProgressExpanded = !inProgressExpanded }
                            )
                        }
                        if (inProgressExpanded) {
                            items(inProgress, key = { "inp_${it.hotel.id}" }) { item ->
                                HotelCard(
                                    hotelWithRoute = item,
                                    status  = HotelStatus.IN_PROGRESS,
                                    onClick = { onOpen(item.hotel) },
                                    onDelete = { viewModel.deleteHotel(userId, item.hotel) }
                                )
                            }
                        }
                    }

                    if (upcoming.isNotEmpty()) {
                        item(key = "header_upcoming") {
                            CollapsibleSectionHeader(
                                title    = strings.bookings + " 🏨",
                                expanded = upcomingExpanded,
                                count    = upcoming.size,
                                onToggle = { upcomingExpanded = !upcomingExpanded }
                            )
                        }
                        if (upcomingExpanded) {
                            items(upcoming, key = { "upc_${it.hotel.id}" }) { item ->
                                HotelCard(
                                    hotelWithRoute = item,
                                    status  = HotelStatus.UPCOMING,
                                    onClick = { onOpen(item.hotel) },
                                    onDelete = { viewModel.deleteHotel(userId, item.hotel) }
                                )
                            }
                        }
                    }

                    if (completed.isNotEmpty()) {
                        item(key = "header_completed") {
                            CollapsibleSectionHeader(
                                title    = strings.bookingExpired,
                                expanded = completedExpanded,
                                count    = completed.size,
                                onToggle = { completedExpanded = !completedExpanded }
                            )
                        }
                        if (completedExpanded) {
                            items(completed, key = { "cmp_${it.hotel.id}" }) { item ->
                                HotelCard(
                                    hotelWithRoute = item,
                                    status  = HotelStatus.COMPLETED,
                                    onClick = { onOpen(item.hotel) },
                                    onDelete = { viewModel.deleteHotel(userId, item.hotel) }
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
private fun HotelCard(
    hotelWithRoute: HotelWithRoute,
    status: HotelStatus,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val hotel   = hotelWithRoute.hotel
    val strings = LocalAppStrings.current
    var menuExpanded     by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text(strings.deleteHotel, fontWeight = FontWeight.Medium) },
            text    = {
                Text(
                    strings.alertHotel + hotel.name +
                            strings.fromHotel + hotelWithRoute.routeName + strings.routeLow
                )
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
                            text     = "🏨",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = hotel.name,
                            fontSize   = 14.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📍", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text     = hotel.address,
                                fontSize = 12.sp,
                                color    = Color(0xFFB0BEC5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text     = strings.route + hotelWithRoute.routeName,
                            fontSize = 12.sp,
                            color    = Color(0xFF6B8FA8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                when (status) {
                    HotelStatus.UPCOMING -> {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3D2B)) {
                            Text(
                                text       = "${"%.0f".format(hotel.totalCost)} ₴",
                                fontSize   = 13.sp,
                                color      = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    HotelStatus.IN_PROGRESS -> {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3A4E)) {
                            Column(
                                modifier            = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text       = "${"%.0f".format(hotel.totalCost)} ₴",
                                    fontSize   = 13.sp,
                                    color      = Color(0xFF4FC3F7),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text       = strings.inProgress,
                                    fontSize   = 11.sp,
                                    color      = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    HotelStatus.COMPLETED -> {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1A3A4E)) {
                            Text(
                                text       = strings.bookingExpired,
                                fontSize   = 13.sp,
                                color      = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Medium,
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
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

            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = Color(0xFF1B3A4B),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier            = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = strings.checkInLet, fontSize = 12.sp, color = Color(0xFF6B8FA8))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text       = hotel.dateFrom,
                            fontSize   = 15.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(
                        modifier            = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = strings.checkOutLet, fontSize = 12.sp, color = Color(0xFF6B8FA8))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text       = hotel.dateTo,
                            fontSize   = 15.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}