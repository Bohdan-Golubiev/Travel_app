package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.model.dataclasses.Place

// ─── Тестові дані ─────────────────────────────────────────────────────────────
val samplePlaces = listOf(
    Place(1, "place 1", "location", "date for arrive"),
    Place(2, "place 2", "location", "date for arrive"),
    Place(3, "place 3", "location", "date for arrive"),
)

// ─── Екран ────────────────────────────────────────────────────────────────────
@Composable
fun RouteDetailScreen(
    onNext: (Place) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(samplePlaces, key = { it.id }) { place ->
                PlaceItem(place = place, onClick = { onNext(place) })
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }

        // ─── Кнопки Edit / Save ───────────────────────────────────────────────
        HorizontalDivider(color = Color(0xFF2A4A5E))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Edit")
            }
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun PlaceItem(place: Place, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = place.name, fontSize = 15.sp, color = Color.White)
                Text(text = place.location, fontSize = 13.sp, color = Color(0xFFB0BEC5))
            }
            Text(text = place.dateForArrive, fontSize = 13.sp, color = Color(0xFFB0BEC5))
        }
    }
}