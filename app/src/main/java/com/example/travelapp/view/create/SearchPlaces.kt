package com.example.travelapp.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.travelapp.view.CreateNavigation
import com.example.travelapp.viewmodel.PlaceItem
import com.example.travelapp.viewmodel.SearchPlacesViewModel

private val CardBackground = Color(0xFFE8E8E8)

@Composable
fun SearchPlaces(
    onSaveRoute: (String) -> Unit,
    viewModel: SearchPlacesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Назва маршруту
        OutlinedTextField(
            value = state.routeName,
            onValueChange = viewModel::onRouteNameChange,
            placeholder = { Text("Route name", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = outlinedTextFieldColors(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Search place", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = outlinedTextFieldColors(),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(state.places) { index, place ->
                PlaceListItem(
                    number = index + 1,
                    place = place,
                    onRemove = { viewModel.removePlace(index) }
                )
            }
        }

        Button(
            onClick = {onSaveRoute(state.routeName)},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
        ) {
            Text(text = "Save", fontSize = 18.sp)
        }
    }
}

@Composable
private fun PlaceListItem(
    number: Int,
    place: PlaceItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "№$number",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.name, color = Color.Black, fontSize = 14.sp)
            Text(text = place.location, color = Color.Gray, fontSize = 12.sp)
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    cursorColor = Color.Black,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black
)