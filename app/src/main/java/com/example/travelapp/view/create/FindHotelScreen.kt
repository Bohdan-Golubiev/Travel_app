package com.example.travelapp.view.create

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.viewmodel.create.FindHotelViewModel
import com.example.travelapp.viewmodel.create.HotelItemState
import com.example.travelapp.viewmodel.create.HotelResult
import com.example.travelapp.viewmodel.create.SaveState
import com.example.travelapp.viewmodel.create.SearchState
import com.example.travelapp.viewmodel.create.SelectedHotelEntry

private val CardBackground    = Color(0xFFCED4DA)
private val SelectedCardColor = Color(0xFFD6EAD6)
private val ButtonBackground  = Color(0xFFD9D9D9)
private val FieldBackground   = Color(0xFFE8E8E8)
private val ErrorColor        = Color(0xFFB00020)

@Composable
fun FindHotelScreen(
    userId      : String,
    routeId     : String,
    onNextClick : (List<SelectedHotelEntry>) -> Unit = {},
    viewModel   : FindHotelViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saveState) {
        if (state.saveState is SaveState.Success) {
            onNextClick(state.selectedHotels)
            viewModel.resetSaveState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value         = state.startPlace,
            onValueChange = viewModel::onStartPlaceChange,
            placeholder   = { Text("Enter city or place", color = Color.Gray) },
            modifier      = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp)),
            shape   = RoundedCornerShape(10.dp),
            singleLine = true,
            colors  = hotelTextFieldColors()
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick   = { viewModel.searchHotels() },
                modifier  = Modifier.weight(1f).height(52.dp),
                shape     = RoundedCornerShape(10.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor   = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                enabled   = state.searchState !is SearchState.Loading
            ) {
                if (state.searchState is SearchState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = Color.Black
                    )
                } else {
                    Text("Search", fontSize = 16.sp)
                }
            }

            Button(
                onClick  = {
                    if (state.selectedHotels.isEmpty()) {
                        onNextClick(emptyList())
                    } else {
                        viewModel.saveHotels(userId, routeId)
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor   = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                enabled   = state.saveState !is SaveState.Loading
            ) {
                if (state.saveState is SaveState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = Color.Black
                    )
                } else {
                    Text("Next ->", fontSize = 16.sp)
                }
            }
        }

        if (state.saveState is SaveState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEDED), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text  = "Save error: ${(state.saveState as SaveState.Error).message}",
                    color = ErrorColor,
                    fontSize = 13.sp
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier            = Modifier.fillMaxWidth()
        ) {

            if (state.selectedHotels.isNotEmpty()) {
                item(key = "selected_header") {
                    Text(
                        text       = "Selected hotels",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                        modifier   = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(state.selectedHotels, key = { "sel_${it.hotel.hotelKey}" }) { entry ->
                    SelectedHotelItem(
                        entry    = entry,
                        onRemove = { viewModel.onRemoveSelected(entry.hotel.hotelKey) }
                    )
                }

                item(key = "selected_divider") {
                    HorizontalDivider(
                        color    = Color.Gray.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            when (val s = state.searchState) {

                is SearchState.Idle -> item(key = "idle") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = "Enter a city and tap Search",
                            color    = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                is SearchState.Loading -> item(key = "loading") {
                    LoadingState(message = "Searching hotels...")
                }

                is SearchState.Error -> item(key = "error") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEDED), RoundedCornerShape(10.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text     = "Error: ${s.message}",
                            color    = ErrorColor,
                            fontSize = 13.sp
                        )
                    }
                }

                is SearchState.Success -> {
                    if (s.hotels.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text     = "No hotels found for this location.",
                                    color    = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        if (state.selectedHotels.isNotEmpty()) {
                            item(key = "results_header") {
                                Text(
                                    text       = "Search results",
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Color.White,
                                    modifier   = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                        items(s.hotels, key = { it.hotelKey }) { hotel ->
                            HotelOptionItem(
                                hotel              = hotel,
                                itemState          = state.itemStates[hotel.hotelKey] ?: HotelItemState(),
                                isAlreadySelected  = state.selectedHotels.any { it.hotel.hotelKey == hotel.hotelKey },
                                onToggleExpand     = { viewModel.toggleExpand(hotel.hotelKey) },
                                onDateFromSelected = { millis -> viewModel.onDateFromSelected(hotel.hotelKey, millis) },
                                onDateToSelected   = { millis -> viewModel.onDateToSelected(hotel.hotelKey, millis) },
                                onAddClick         = { viewModel.onAddClick(hotel) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedHotelItem(
    entry   : SelectedHotelEntry,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SelectedCardColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = entry.hotel.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.Black
            )
            Text(
                text     = "${entry.dateFrom} – ${entry.dateTo}  •  ${entry.days} days",
                fontSize = 12.sp,
                color    = Color.DarkGray
            )
            Text(
                text     = "Total: ${entry.totalCost} $",
                fontSize = 12.sp,
                color    = Color.Black
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector        = Icons.Filled.Close,
                contentDescription = "Remove",
                tint               = Color.DarkGray
            )
        }
    }
}

@Composable
private fun HotelOptionItem(
    hotel             : HotelResult,
    itemState         : HotelItemState,
    isAlreadySelected : Boolean,
    onToggleExpand    : () -> Unit,
    onDateFromSelected: (Long) -> Unit,
    onDateToSelected  : (Long) -> Unit,
    onAddClick        : () -> Unit
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }

    if (showFromPicker) {
        DatePickerModal(
            onDateSelected = { it?.let(onDateFromSelected) },
            onDismiss      = { showFromPicker = false }
        )
    }
    if (showToPicker) {
        DatePickerModal(
            onDateSelected = { it?.let(onDateToSelected) },
            onDismiss      = { showToPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isAlreadySelected) SelectedCardColor else CardBackground,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = hotel.name,
                    fontSize   = 15.sp,
                    color      = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
                if (hotel.address.isNotEmpty()) {
                    Text(text = hotel.address, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text     = "Cost per day: ${hotel.cost} $",
                fontSize = 14.sp,
                color    = Color.Black,
                modifier = Modifier.weight(1f)
            )
            SmallSquareButton(
                label   = if (itemState.isExpanded) "∧" else "V",
                onClick = onToggleExpand
            )
        }

        AnimatedVisibility(
            visible = itemState.isExpanded,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.4f))
                Text("Select dates", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                DateRow(label = "From:", value = itemState.dateFrom, onClick = { showFromPicker = true })
                DateRow(label = "To:",   value = itemState.dateTo,   onClick = { showToPicker = true })

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text     = "Duration: ${if (itemState.days > 0) "${itemState.days} days" else "—"}",
                            fontSize = 13.sp,
                            color    = Color.Black
                        )
                        Text(
                            text     = "Total cost: ${itemState.days * hotel.cost} $",
                            fontSize = 13.sp,
                            color    = Color.Black
                        )
                    }
                    if (itemState.days > 0) {
                        Button(
                            onClick         = onAddClick,
                            shape           = RoundedCornerShape(8.dp),
                            colors          = ButtonDefaults.buttonColors(
                                containerColor = if (isAlreadySelected) Color(0xFF388E3C) else Color(0xFF4CAF50),
                                contentColor   = Color.White
                            ),
                            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier        = Modifier.height(36.dp)
                        ) {
                            Text(
                                text     = if (isAlreadySelected) "Update" else "Add to trip",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRow(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = Color.Black, modifier = Modifier.width(48.dp))
        OutlinedButton(
            onClick         = onClick,
            modifier        = Modifier.fillMaxWidth().height(44.dp),
            shape           = RoundedCornerShape(6.dp),
            colors          = ButtonDefaults.outlinedButtonColors(
                containerColor = FieldBackground,
                contentColor   = Color.Black
            ),
            border          = BorderStroke(0.dp, Color.Transparent),
            contentPadding  = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text     = value.ifEmpty { "00.00.0000" },
                color    = if (value.isEmpty()) Color.Gray else Color.Black,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss     : () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun SmallSquareButton(label: String, onClick: () -> Unit, tint: Color = Color.Black) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(ButtonBackground, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Text(text = label, fontSize = 16.sp, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoadingState(message: String) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = Color.DarkGray)
        Text(text = message, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun hotelTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    cursorColor             = Color.Black,
    focusedTextColor        = Color.Black,
    unfocusedTextColor      = Color.Black
)