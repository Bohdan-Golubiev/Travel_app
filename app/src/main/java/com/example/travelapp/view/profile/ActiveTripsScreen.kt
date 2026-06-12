package com.example.travelapp.view.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.ActiveTripItem
import com.example.travelapp.viewmodel.profile.ActiveTripsViewModel

@Composable
fun ActiveTripsScreen(
    userId: String,
    viewModel: ActiveTripsViewModel = viewModel()
) {
    val trips by viewModel.getActiveTrips(userId).collectAsState(initial = emptyList())
    val strings = LocalAppStrings.current

    when {
        trips.isEmpty() -> ActiveTripsEmpty(strings)
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trips, key = { it.route.id }) { item ->
                    ActiveTripCard(item, strings)
                }
            }
        }
    }
}

@Composable
private fun ActiveTripCard(item: ActiveTripItem, strings: AppStrings) {
    val route = item.route
    val nextPlace = item.nextPlace

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3550)),
        border = BorderStroke(1.dp, Color(0xFF219EBC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFF219EBC),
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (route.description.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = route.description,
                            fontSize = 13.sp,
                            color = Color(0xFFB0BEC5),
                            maxLines = 2
                        )
                    }
                }
                if (route.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(16.dp)
                    )                }
            }

            if (nextPlace != null) {
                HorizontalDivider(color = Color(0xFF2A4A5E), thickness = 1.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF219EBC),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = strings.nextLocation,
                        fontSize = 11.sp,
                        color = Color(0xFF219EBC),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = nextPlace.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = strings.plannedDate + nextPlace.visitDate,
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF2A4A5E), thickness = 1.dp)
            TripProgressBar(
                progressPercent = item.progressPercent,
                visitedCount = item.visitedCount,
                totalCount = item.totalCount,
                strings = strings
            )
        }
    }
}

@Composable
private fun TripProgressBar(
    progressPercent: Int,
    visitedCount: Int,
    totalCount: Int,
    strings: AppStrings
) {
    val progressColor = when {
        progressPercent >= 100 -> Color(0xFF4CAF50)
        progressPercent >= 60  -> Color(0xFF219EBC)
        progressPercent >= 30  -> Color(0xFFFB8500)
        else                   -> Color(0xFF219EBC)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "tripProgress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.routeProgress,
                fontSize = 11.sp,
                color = Color(0xFF219EBC),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (totalCount > 0) {
                    Text(
                        text = "$visitedCount / $totalCount" + strings.places,
                        fontSize = 11.sp,
                        color = Color(0xFF90A4AE)
                    )
                    Text(
                        text = "·",
                        fontSize = 11.sp,
                        color = Color(0xFF2A4A5E)
                    )
                }
                Text(
                    text = "$progressPercent%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = progressColor
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = Color(0xFF2A4A5E),
        )
    }
}

@Composable
private fun ActiveTripsEmpty(strings: AppStrings) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF2A4A5E),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = strings.noActiveTrips,
                fontSize = 15.sp,
                color = Color(0xFF607D8B)
            )
        }
    }
}