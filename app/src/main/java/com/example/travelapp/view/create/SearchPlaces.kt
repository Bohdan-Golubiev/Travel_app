package com.example.travelapp.view.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.view.profile.routes.PlaceInfoBottomSheet
import com.example.travelapp.viewmodel.create.PlaceItem
import com.example.travelapp.viewmodel.create.SearchPlacesViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val CardBackground = Color(0xFFE8E8E8)
private val SuggestionBackground = Color(0xFFFFFFFF)

@Composable
fun SearchPlaces(
    userId: String,
    onSaveRoute: (String, String) -> Unit,
    viewModel: SearchPlacesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    var selectedPlace by remember { mutableStateOf<PlaceItem?>(null) }

    selectedPlace?.let { place ->
        PlaceInfoBottomSheet(
            place = place,
            currentUserId = userId,
            onDismiss = { selectedPlace = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.routeName,
                onValueChange = viewModel::onRouteNameChange,
                placeholder = { Text(strings.routeName, color = Color.Gray) },
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
                placeholder = { Text(strings.searchPlace, color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = outlinedTextFieldColors(),
                singleLine = true,
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF219EBC)
                        )
                    }
                }
            )
            OutlinedTextField(
                value = state.routeDescription,
                onValueChange = viewModel::onRouteDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        text = strings.writeDescription,
                        fontSize = 14.sp,
                        color = Color(0xFF5E7A8A)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF219EBC),
                    unfocusedBorderColor = Color(0xFF2A4A5E),
                    cursorColor = Color(0xFF219EBC)
                ),
                supportingText = {
                    Text(
                        text = "${state.routeDescription.length}/1000",
                        fontSize = 12.sp,
                        color = Color(0xFF5E7A8A)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                maxLines = 8
            )

            val lazyListState = rememberLazyListState()
            val reorderableLazyListState = rememberReorderableLazyListState(
                lazyListState = lazyListState
            ) { from, to ->
                viewModel.movePlace(from.index, to.index)
            }

            //Список
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(state.places, key = { _, place -> place.id }) { index, place ->
                    ReorderableItem(reorderableLazyListState, key = place.id) { isDragging ->
                        PlaceListItem(
                            number = index + 1,
                            place = place,
                            onRemove = { viewModel.removePlace(index) },
                            onDateChange = { date -> viewModel.updatePlaceDate(index, date) },
                            onInfoClick = { selectedPlace = place },
                            dragHandle = {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Drag",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .draggableHandle()
                                        .size(24.dp)
                                )
                            },
                            isDragging = isDragging
                        )
                    }
                }
            }

            if (state.routeName.isNotBlank() && state.places.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.saveRoute(userId) { routeId ->
                            onSaveRoute(state.routeName, routeId)
                        }
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = strings.save, fontSize = 18.sp)
                    }
                }
            }
        }

        // Dropdown і помилка
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp)
                .zIndex(1f)
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            if (state.suggestions.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    shadowElevation = 8.dp,
                    color = SuggestionBackground
                ) {
                    Column {
                        state.suggestions.forEach { prediction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSuggestionSelected(prediction) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = prediction.getPrimaryText(null).toString(),
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = prediction.getSecondaryText(null).toString(),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaceListItem(
    number: Int,
    place: PlaceItem,
    onRemove: () -> Unit,
    onDateChange: (String) -> Unit,
    onInfoClick: () -> Unit,
    dragHandle: @Composable () -> Unit,
    isDragging: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDragging) Color(0xFFD0D0D0) else CardBackground,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dragHandle()

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "№$number",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.name, color = Color.Black, fontSize = 14.sp)
            Text(text = place.location, color = Color.Gray, fontSize = 12.sp)
            DateRow(
                label = strings.date,
                value = place.visitDate,
                onClick = { showDatePicker = true }
            )
        }
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Деталі",
                tint = Color(0xFF219EBC)
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = strings.remove,
                tint = Color.DarkGray
            )
        }
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { millis ->
                    if (millis != null) {
                        val formatted = java.text.SimpleDateFormat(
                            "dd.MM.yyyy",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(millis))
                        onDateChange(formatted)
                    }
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun DateRow(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = Color.Black, modifier = Modifier.width(40.dp))
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(30.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFE8E8E8),
                contentColor = Color.Black
            ),
            border = BorderStroke(0.dp, Color.Transparent),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = value.ifEmpty { "00.00.0000" },
                color = if (value.isEmpty()) Color.Gray else Color.Black,
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
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
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
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    ) {
        DatePicker(state = datePickerState)
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