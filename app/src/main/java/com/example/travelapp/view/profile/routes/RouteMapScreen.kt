package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.viewmodel.create.routes.RouteDetailViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RouteMapScreen(routeId: String, viewModel: RouteDetailViewModel = viewModel()) {
    val places by viewModel.getPlaces(routeId).collectAsState(initial = emptyList())
    val validPlaces = places.filter { it.latitude != null && it.longitude != null }

    val cameraState = rememberCameraPositionState()

    LaunchedEffect(validPlaces) {
        if (validPlaces.isNotEmpty()) {
            val first = validPlaces.first()
            cameraState.position = CameraPosition.fromLatLngZoom(
                LatLng(first.latitude!!, first.longitude!!),
                8f
            )
        }
    }

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
        if (validPlaces.size > 1) {
            Polyline(
                points = validPlaces.map { LatLng(it.latitude!!, it.longitude!!) },
                color = Color(0xFF219EBC),
                width = 6f
            )
        }
    }
}