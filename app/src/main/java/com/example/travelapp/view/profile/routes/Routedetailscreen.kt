package com.example.travelapp.view.profile.routes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.utils.LocalAppStrings
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
    routeDescription: String,
    userId: String,
    onNext: (PlaceEntity) -> Unit,
    onMakeBooking: () -> Unit,
    onTitleChange: (String, String) -> Unit,
    viewModel: RouteDetailViewModel = viewModel()
) {
    val places by viewModel.getPlaces(routeId).collectAsState(initial = emptyList())
    val isEditing by viewModel.isEditing.collectAsState()
    val editedName by viewModel.editedName.collectAsState()
    val editedDescription by viewModel.editedDescription.collectAsState()
    val editedPlaces by viewModel.editedPlaces.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timelineError by viewModel.timelineError.collectAsState()
    val editedIsFavorite by viewModel.editedIsFavorite.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    val displayedPlaces = if (isEditing) editedPlaces else places

    val lazyListState = rememberLazyListState()
    val placesHeaderCount = 3
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState
    ) { from, to ->
        viewModel.movePlace(
            from.index - placesHeaderCount,
            to.index - placesHeaderCount
        )
    }
    val strings = LocalAppStrings.current

    var routeInfoExpanded by remember { mutableStateOf(true) }
    var searchFieldOffsetY by remember { mutableIntStateOf(0) }

    if (timelineError) {
        AlertDialog(
            onDismissRequest = viewModel::dismissTimelineError,
            title = { Text(strings.inCorrectDate) },
            text = { Text(strings.inCorrectDateMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissTimelineError) {
                    Text(strings.understand, color = Color(0xFF219EBC))
                }
            },
            containerColor = Color(0xFF1B3A4B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0BEC5)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isEditing) 64.dp else 56.dp, top = 6.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {

            //блок про маршрут
            item(key = "route_info_header") {
                HorizontalDivider(color = Color(0xFF2A4A5E))
                if (isEditing) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { routeInfoExpanded = !routeInfoExpanded },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.aboutRoute,
                                fontSize = 14.sp,
                                color = Color(0xFFB0BEC5)
                            )
                            Icon(
                                imageVector = if (routeInfoExpanded)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFFB0BEC5)
                            )
                        }
                    }
                } else if (routeDescription.isNotBlank()) {
                    Text(
                        text = routeDescription,
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider(color = Color(0xFF2A4A5E))
                }
            }

            //контент про маршрут
            if (isEditing) {
                item(key = "route_info_content") {
                    AnimatedVisibility(
                        visible = routeInfoExpanded,
                        enter = expandVertically(animationSpec = tween(200)),
                        exit = shrinkVertically(animationSpec = tween(200))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = viewModel::onNameChange,
                                    placeholder = {
                                        Text(strings.newRouteName, color = Color.Gray)
                                    },
                                    supportingText = {
                                        Text(strings.routeName, color = Color.Gray)
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF219EBC),
                                        unfocusedBorderColor = Color(0xFF2A4A5E),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color(0xFF219EBC)
                                    )
                                )
                                IconButton(onClick = viewModel::onFavoriteToggle) {
                                    Icon(
                                        imageVector = if (editedIsFavorite)
                                            Icons.Default.Favorite
                                        else
                                            Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (editedIsFavorite) Color(0xFFFF0000)
                                        else Color.Gray,
                                        modifier = Modifier
                                            .size(30.dp)
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = editedDescription,
                                onValueChange = viewModel::onDescriptionChange,
                                placeholder = {
                                    Text(strings.descriptionOpt, color = Color.Gray)
                                },
                                supportingText = {
                                    Text(strings.description, color = Color.Gray)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 80.dp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF219EBC),
                                    unfocusedBorderColor = Color(0xFF2A4A5E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF219EBC)
                                )
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFF2A4A5E))
                }

                //поле пошуку
                item(key = "place_search") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                searchFieldOffsetY = coords.positionInRoot().y.toInt()
                            }
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = {
                                Text(strings.searchPlace, color = Color.Gray)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF219EBC),
                                unfocusedBorderColor = Color(0xFF2A4A5E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF219EBC)
                            ),
                            trailingIcon = {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF219EBC)
                                    )
                                } else if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = viewModel::clearSearch) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        )
                        searchError?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 20.dp, top = 2.dp, bottom = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFF2A4A5E))
                }
            }

            //Список місць
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
                            onDateChange = { date -> viewModel.updatePlaceDate(index, date) }
                        )
                    }
                } else {
                    PlaceItem(place = place, onClick = { onNext(place) })
                }
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }

        //кнопки скасування/збереження
        if (isEditing) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::cancelEditing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(strings.cancel)
                }
                Button(
                    onClick = {
                        viewModel.saveChanges(userId, routeId, places) { newName, newDescription ->
                            onTitleChange(newName, newDescription)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(strings.save)
                    }
                }
            }
        }

        //кнопки додання/редагування
        if (!isEditing) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                HorizontalDivider(color = Color(0xFF2A4A5E))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMakeBooking,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF219EBC))
                    ) {
                        Text(strings.addBookings)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.startEditing(
                                routeName,
                                routeDescription,
                                places,
                                routeId
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(strings.edit)
                    }
                }
            }
        }

        //підказки при пошуку в редагуванні
        if (isEditing && suggestions.isNotEmpty()) {
            val density = LocalDensity.current
            val topOffsetDp = with(density) {
                (searchFieldOffsetY + 20).toDp()
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .absoluteOffset(y = topOffsetDp)
                    .zIndex(10f),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                shadowElevation = 12.dp,
                color = Color(0xFF1B3A4B)
            ) {
                Column {
                    suggestions.forEach { prediction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onSuggestionSelected(prediction, routeId)
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = prediction.getSecondaryText(null).toString(),
                                    fontSize = 12.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFF2A4A5E))
                    }
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
        DateRow(
            value = place.visitDate,
            onClick = { showDatePicker = true }
        )
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
            color = Color.Black,
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