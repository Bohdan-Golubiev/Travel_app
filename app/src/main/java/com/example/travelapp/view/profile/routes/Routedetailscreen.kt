package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.viewmodel.profile.RouteDetailViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RouteDetailScreen(
    routeId: String,
    routeName: String,
    userId: String,
    onNext: (PlaceEntity) -> Unit,
    onTitleChange: (String) -> Unit = {},
    viewModel: RouteDetailViewModel = viewModel()
) {
    val places by viewModel.getPlaces(routeId).collectAsState(initial = emptyList())
    val isEditing by viewModel.isEditing.collectAsState()
    val editedName by viewModel.editedName.collectAsState()
    val editedPlaces by viewModel.editedPlaces.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val displayedPlaces = if (isEditing) editedPlaces else places

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState
    ) { from, to ->
        viewModel.movePlace(from.index, to.index)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        if (isEditing) {
            OutlinedTextField(
                value = editedName,
                onValueChange = viewModel::onNameChange,
                placeholder = { Text("Route name", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF219EBC),
                    unfocusedBorderColor = Color(0xFF2A4A5E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF219EBC)
                )
            )
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(displayedPlaces, key = { _, place -> place.id }) { index, place ->
                if (isEditing) {
                    ReorderableItem(reorderableLazyListState, key = place.id) { isDragging ->
                        EditablePlaceItem(
                            number = index + 1,
                            place = place,
                            isDragging = isDragging,
                            onRemove = { viewModel.removePlace(index) },
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
                            onDateChange = { date -> viewModel.updatePlaceDate(index, date) },
                        )
                    }
                } else {
                    PlaceItem(place = place, onClick = { onNext(place) })
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }

        HorizontalDivider(color = Color(0xFF2A4A5E))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (isEditing) viewModel.cancelEditing()
                    else viewModel.startEditing(routeName, places)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(if (isEditing) "Cancel" else "Edit")
            }
            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.saveChanges(userId, routeId, places) {
                            onTitleChange(editedName)
                        }
                    }
                },
                enabled = isEditing && !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditing) Color(0xFF219EBC) else Color(0xFF2A4A5E)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun EditablePlaceItem(
    number: Int,
    place: PlaceEntity,
    isDragging: Boolean,
    onRemove: () -> Unit,
    dragHandle: @Composable () -> Unit,
    onDateChange: (String) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDragging) Color(0xFF1B3A4B) else Color(0xFF0D1B2A),
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
            Text(text = place.name, color = Color.White, fontSize = 14.sp)
            Text(text = place.location, color = Color(0xFFB0BEC5), fontSize = 12.sp)
        }
        if (place.visitDate.isNotEmpty()) {
            DateRow(
                value = place.visitDate,
                onClick = { showDatePicker = true }
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFFB0BEC5)
            )
        }
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { millis ->
                    if (millis != null) {
                        val formatted = SimpleDateFormat(
                            "dd.MM.yyyy",
                            Locale.getDefault()
                        ).format(Date(millis))
                        onDateChange(formatted)
                    }
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun PlaceItem(place: PlaceEntity, onClick: () -> Unit) {
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
            Text(text = place.visitDate, fontSize = 13.sp, color = Color(0xFFB0BEC5))
        }
    }
}
@Composable
private fun DateRow(value: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(30.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF219EBC),
            contentColor = Color.Black
        ),
        border = BorderStroke(0.dp, Color.Transparent),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            text = value.ifEmpty { "00.00.0000" },
            color = if (value.isEmpty()) Color.Gray else Color.Black,
            fontSize = 13.sp
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
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