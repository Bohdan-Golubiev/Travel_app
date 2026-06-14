package com.example.travelapp.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val status: String,
    val cost: Double,
    val from: String,
    val to  :String
)

private val CardBackground    = Color(0xFFF5F5F5)
private val CardBorder        = Color(0xFFE0E0E0)
private val SelectedCardBg    = Color(0xFFF4FAF0)
private val SelectedBorder    = Color(0xFF3B6D11)
private val SelectedIconBg    = Color(0xFFC0DD97)
private val SelectedIconColor = Color(0xFF3B6D11)
private val RouteBg           = Color(0xFFEEEEEE)
private val RouteSelectedBg   = Color(0xFFEAF3DE)
private val StatusConfirmedBg = Color(0xFFEAF3DE)
private val StatusConfirmedFg = Color(0xFF3B6D11)
private val ButtonBackground  = Color(0xFFEEEEEE)
private val IconBg            = Color(0xFFEEEEEE)

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
                    SectionLabel(text = strings.selected)
                }
                items(state.selectedServices) { option ->
                    BookingOptionCard(
                        option = option,
                        isSelected = true,
                        onAddClick = { viewModel.onAddClick(option) }
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(CardBackground, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.DarkGray)
                    }
                }
            } else {
                if (state.results.isNotEmpty()) {
                    item { SectionLabel(text = strings.searchResult) }
                }
                items(state.results) { option ->
                    BookingOptionCard(
                        option = option,
                        isSelected = state.selectedServices.contains(option),
                        onAddClick = { viewModel.onAddClick(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingOptionCard(
    option: BookingOption,
    isSelected: Boolean,
    onAddClick: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SelectedCardBg else CardBackground)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) SelectedBorder else CardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSelected) SelectedIconBg else IconBg,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✈", fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = option.name,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.Black
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.padding(top = 3.dp)
                ) {
                    MetaChip(icon = "⏱", text = option.time)
                    MetaChip(icon = "📅", text = option.date)
                }
            }

            SelectToggleButton(isSelected = isSelected, onClick = onAddClick)
        }

        RouteRow(from = option.from, to = option.to, isSelected = isSelected)

        HorizontalDivider(color = if (isSelected) SelectedIconBg else CardBorder)

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text     = strings.cost,
                    fontSize = 11.sp,
                    color    = Color.Gray
                )
                Text(
                    text       = "${option.cost.toLong()} ₴",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.Black
                )
            }
            StatusBadge(status = option.status, isSelected = isSelected)
        }
    }
}

@Composable
private fun SelectToggleButton(isSelected: Boolean, onClick: () -> Unit) {
    val icon: ImageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add
    Box(
        modifier         = Modifier
            .size(34.dp)
            .background(
                color = if (isSelected) SelectedBorder else ButtonBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .border(0.5.dp, if (isSelected) SelectedBorder else CardBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector        = icon,
                contentDescription = if (isSelected) "Видалити" else "Додати",
                tint               = if (isSelected) Color.White else Color.DarkGray,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}
@Composable
private fun RouteRow(from: String, to: String, isSelected: Boolean) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) RouteSelectedBg else RouteBg,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text       = from,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isSelected) SelectedIconColor else Color.DarkGray
        )
        Box(modifier = Modifier.size(6.dp).background(
            if (isSelected) SelectedIconColor else Color.Gray,
            RoundedCornerShape(50)
        ))
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = if (isSelected) Color(0xFF97C459) else Color.LightGray,
            thickness = 1.dp
        )
        Text(
            text     = "→",
            fontSize = 18.sp,
            color    = if (isSelected) SelectedIconColor else Color.Gray
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = if (isSelected) Color(0xFF97C459) else Color.LightGray,
            thickness = 1.dp
        )
        Box(modifier = Modifier.size(6.dp).background(
            if (isSelected) SelectedIconColor else Color.Gray,
            RoundedCornerShape(50)
        ))
        Text(
            text       = to,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isSelected) SelectedIconColor else Color.DarkGray
        )
    }
}
@Composable
private fun StatusBadge(status: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) StatusConfirmedBg else ButtonBackground,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text     = status,
            fontSize = 12.sp,
            color    = if (isSelected) StatusConfirmedFg else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
private fun MetaChip(icon: String, text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = icon, fontSize = 12.sp)
        Text(text = text, fontSize = 12.sp, color = Color.Gray)
    }
}
@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        fontSize   = 12.sp,
        fontWeight = FontWeight.Medium,
        color      = Color.White,
        letterSpacing = 0.5.sp,
        modifier   = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
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
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = Color.Gray) },
            modifier      = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp)),
            shape      = RoundedCornerShape(10.dp),
            singleLine = true,
            colors     = vehicleTextFieldColors()
        )

        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
                    .background(CardBackground, RoundedCornerShape(10.dp))
                    .border(0.5.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                suggestions.forEach { airport ->
                    Text(
                        text     = "${airport.city} (${airport.iata}) — ${airport.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAirportSelected(airport) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        fontSize = 13.sp,
                        color    = Color.Black
                    )
                    HorizontalDivider(color = CardBorder)
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