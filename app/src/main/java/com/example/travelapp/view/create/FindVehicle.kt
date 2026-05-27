package com.example.travelapp.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.model.dataclasses.Airport
import com.example.travelapp.viewmodel.create.FindVehicleViewModel
import com.example.travelapp.utils.LocalAppStrings

data class BookingOption(
    val name: String,
    val time: String,
    val date: String,
    val cost: String,
    val from: String,
    val to  :String
)
private val CardBackground   = Color(0xFFCED4DA)
private val SelectedCardColor = Color(0xFFD6EAD6)
private val ButtonBackground = Color(0xFFD9D9D9)

@Composable
fun FindVehicleScreen(
    userId: String,
    routeId: String,
    onNextClick: (List<BookingOption>) -> Unit = {},
    viewModel: FindVehicleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        PlaceInputWithSuggestions(
            value = state.startPlace,
            placeholder = strings.startPlace,
            suggestions = state.startSuggestions,
            onValueChange = viewModel::onStartPlaceChange,
            onAirportSelected = viewModel::onStartAirportSelected
        )

        PlaceInputWithSuggestions(
            value = state.endPlace,
            placeholder = strings.endPlace,
            suggestions = state.endSuggestions,
            onValueChange = viewModel::onEndPlaceChange,
            onAirportSelected = viewModel::onEndAirportSelected
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Button(
                onClick = { viewModel.onSearchClick() },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text(strings.search, fontSize = 16.sp) }

            Button(
                onClick = {
                    viewModel.onNextClick(
                        userId  = userId,
                        routeId = routeId,
                        onDone  = { onNextClick(state.selectedServices) }
                    )
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text(strings.next + "→", fontSize = 16.sp) }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            if (state.selectedServices.isNotEmpty()) {
                item {
                    Text(
                        text = strings.selected,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                items(state.selectedServices) { option ->
                    BookingOptionItem(
                        option = option,
                        isSelected = true,
                        onAddClick = { viewModel.onAddClick(option) }
                    )
                }
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(CardBackground, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                if (state.results.isNotEmpty()) {
                    item {
                        Text(
                            text = strings.searchResult,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                items(state.results) { option ->
                    val isSelected = state.selectedServices.contains(option)

                    BookingOptionItem(
                        option = option,
                        isSelected = isSelected,
                        onAddClick = { viewModel.onAddClick(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingOptionItem(
    option: BookingOption,
    isSelected: Boolean,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) SelectedCardColor else CardBackground,
                RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color.DarkGray else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = option.name,
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(text = option.time, fontSize = 13.sp, color = Color.Black)
            Text(text = option.date, fontSize = 13.sp, color = Color.Black)
            Text(text = option.cost, fontSize = 13.sp, color = Color.Black)
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) Color.DarkGray else ButtonBackground,
                    RoundedCornerShape(8.dp)
                )
                .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onAddClick, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (isSelected) "✓" else "+",
                    fontSize = 22.sp,
                    color = if (isSelected) Color.White else Color.Black
                )
            }
        }
    }
}
@Composable
private fun PlaceInputWithSuggestions(
    value: String,
    placeholder: String,
    suggestions: List<Airport>,
    onValueChange: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit
) {
    Box {
        Column {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = vehicleTextFieldColors()
            )
        }

        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
                    .background(CardBackground, RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            ) {
                suggestions.forEach { airport ->
                    Text(
                        text = "${airport.city} (${airport.iata}) — ${airport.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAirportSelected(airport) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                }
            }
        }
    }
}
@Composable
private fun vehicleTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    cursorColor             = Color.Black,
    focusedTextColor        = Color.Black,
    unfocusedTextColor      = Color.Black
)