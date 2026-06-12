package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.create.routes.RouteMapViewModel
import com.example.travelapp.viewmodel.create.routes.RouteSegmentInfo
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RouteMapScreen(routeId: String, viewModel: RouteMapViewModel = viewModel()) {
    val places by viewModel.getPlaces(routeId).collectAsState(initial = emptyList())
    val validPlaces = places.filter { it.latitude != null && it.longitude != null }
    val routeSegments by viewModel.routeSegments.collectAsState()
    val selectedSegmentIndex by viewModel.selectedSegmentIndex.collectAsState()
    val travelMode by viewModel.travelMode.collectAsState()
    val isLoadingRoute by viewModel.isLoadingRoute.collectAsState()
    val cameraState = rememberCameraPositionState()
    val strings = LocalAppStrings.current

    LaunchedEffect(validPlaces, travelMode) {
        if (validPlaces.isNotEmpty()) {
            cameraState.position = CameraPosition.fromLatLngZoom(
                LatLng(validPlaces.first().latitude!!, validPlaces.first().longitude!!), 8f
            )
            viewModel.fetchRoutePolylines(validPlaces)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState
        ) {
            validPlaces.forEachIndexed { index, place ->
                Marker(
                    state = MarkerState(LatLng(place.latitude!!, place.longitude!!)),
                    title = "${index + 1}. ${place.name}",
                )
            }

            routeSegments.forEachIndexed { index, segment ->
                val isSelected = selectedSegmentIndex == index
                Polyline(
                    points = segment.points,
                    color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFE91E63),
                    width = if (isSelected) 12f else 10f,
                    clickable = true,
                    onClick = { viewModel.onSegmentClick(index) }
                )
            }

            if (routeSegments.isEmpty() && validPlaces.size > 1) {
                Polyline(
                    points = validPlaces.map { LatLng(it.latitude!!, it.longitude!!) },
                    color = Color(0xFF219EBC).copy(alpha = 0.4f),
                    width = 6f
                )
            }
        }

        selectedSegmentIndex?.let { index ->
            val segment = routeSegments.getOrNull(index)
            val fromPlace = validPlaces.getOrNull(index)
            val toPlace = validPlaces.getOrNull(index + 1)

            if (segment != null && fromPlace != null && toPlace != null) {
                SegmentInfoCard(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    fromName = fromPlace.name,
                    toName = toPlace.name,
                    segment = segment,
                    onDismiss = { viewModel.dismissSegmentInfo() }
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TravelModeButton(
                label = strings.car,
                selected = travelMode == RouteMapViewModel.TravelMode.DRIVING,
                onClick = { viewModel.setTravelMode(RouteMapViewModel.TravelMode.DRIVING) }
            )
            TravelModeButton(
                label = strings.walk,
                selected = travelMode == RouteMapViewModel.TravelMode.WALKING,
                onClick = { viewModel.setTravelMode(RouteMapViewModel.TravelMode.WALKING) }
            )
        }

        if (isLoadingRoute) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun TravelModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF219EBC) else Color.Transparent
    val contentColor = if (selected) Color.White else Color.Gray

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = contentColor, fontSize = 13.sp)
    }
}
@Composable
fun SegmentInfoCard(
    modifier: Modifier = Modifier,
    fromName: String,
    toName: String,
    segment: RouteSegmentInfo,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$fromName → $toName",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрити",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismiss() },
                    tint = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                InfoChip(icon = "📍", value = formatDistance(segment.distanceMeters, strings))
                InfoChip(icon = "⏱", value = formatDuration(segment.durationSeconds, strings ))
            }
        }
    }
}

@Composable
private fun InfoChip(icon: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(value, fontSize = 14.sp, color = Color(0xFF219EBC), fontWeight = FontWeight.Medium)
    }
}
@Composable
fun formatDistance(meters: Int, strings: AppStrings): String {
    return if (meters >= 1000) {
        val km = meters / 1000.0
        val formatted = if (km == km.toLong().toDouble()) {
            km.toLong().toString()
        } else {
            String.format("%.1f", km)
        }
        "$formatted ${strings.km}"
    } else {
        "$meters ${strings.meters}"
    }
}

@Composable
fun formatDuration(seconds: Int, strings: AppStrings): String {
    val days = seconds / (24 * 3600)
    val hours = (seconds % (24 * 3600)) / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        days > 0 && hours > 0 ->
            "$days ${strings.days} $hours ${strings.hours}"

        days > 0 ->
            "$days ${strings.days}"

        hours > 0 && minutes > 0 ->
            "$hours ${strings.hours} $minutes ${strings.minutes}"

        hours > 0 ->
            "$hours ${strings.hours}"

        else ->
            "$minutes ${strings.minutes}"
    }
}