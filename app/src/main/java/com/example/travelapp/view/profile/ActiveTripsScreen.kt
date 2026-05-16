package com.example.travelapp.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.viewmodel.profile.ActiveTripItem
import com.example.travelapp.viewmodel.profile.ActiveTripsViewModel

@Composable
fun ActiveTripsScreen(
    userId: String,
    viewModel: ActiveTripsViewModel = viewModel()
) {
    val trips by viewModel.getActiveTrips(userId).collectAsState(initial = emptyList())

    when {
        trips.isEmpty() -> ActiveTripsEmpty()
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trips, key = { it.route.id }) { item ->
                    ActiveTripCard(item)
                }
            }
        }
    }
}

@Composable
private fun ActiveTripCard(item: ActiveTripItem) {
    val route = item.route
    val nextPlace = item.nextPlace

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3550)),
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
                    Text(text = "★", fontSize = 18.sp, color = Color(0xFFFFC107))
                }
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
                        text = "Наступна локація",
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
                        text = nextPlace.visitDate,
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTripsEmpty() {
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
                text = "Немає активних подорожей",
                fontSize = 15.sp,
                color = Color(0xFF607D8B)
            )
        }
    }
}