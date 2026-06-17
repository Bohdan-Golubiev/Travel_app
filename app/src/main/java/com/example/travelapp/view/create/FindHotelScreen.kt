package com.example.travelapp.view.create

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.utils.toMessage
import com.example.travelapp.viewmodel.create.FindHotelViewModel
import com.example.travelapp.viewmodel.create.HotelItemState
import com.example.travelapp.viewmodel.create.HotelResult
import com.example.travelapp.viewmodel.create.SaveState
import com.example.travelapp.viewmodel.create.SearchError
import com.example.travelapp.viewmodel.create.SearchState
import com.example.travelapp.viewmodel.create.SelectedHotelEntry

private val CardBackground    = Color(0xFFF5F5F5)
private val CardBorder        = Color(0xFFE0E0E0)
private val SelectedCardBg    = Color(0xFFF4FAF0)
private val SelectedBorder    = Color(0xFF3B6D11)
private val SelectedChipBg    = Color(0xFFEAF3DE)
private val SelectedChipFg    = Color(0xFF3B6D11)
private val SelectedChipBorder = Color(0xFFC0DD97)
private val ExpandedBg        = Color(0xFFEEEEEE)
private val ExpandedSelectedBg = Color(0xFFF0F9E8)
private val ButtonBackground  = Color(0xFFEEEEEE)
private val AddGreen          = Color(0xFF639922)
private val AddGreenDark      = Color(0xFF3B6D11)
private val FieldBackground   = Color(0xFFFFFFFF)
private val ErrorColor        = Color(0xFFB00020)

@Composable
fun FindHotelScreen(
    userId      : String,
    routeId     : String,
    onNextClick : (List<SelectedHotelEntry>) -> Unit = {},
    viewModel   : FindHotelViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    var fullScreenBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(state.saveState) {
        if (state.saveState is SaveState.Success) {
            onNextClick(state.selectedHotels)
            viewModel.resetSaveState()
        }
    }

    fullScreenBitmap?.let { bitmap ->
        FullScreenPhotoDialog(bitmap = bitmap, onDismiss = { fullScreenBitmap = null })
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
            placeholder   = { Text(strings.enterCity, color = Color.Gray) },
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
                    Text(strings.search, fontSize = 16.sp)
                }
            }

            Button(
                onClick = {
                    if (state.selectedHotels.isEmpty()) onNextClick(emptyList())
                    else viewModel.saveHotels(userId, routeId)
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
                    Text(strings.next + " →", fontSize = 16.sp)
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
                    text  = strings.saveError + (state.saveState as SaveState.Error).message,
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
                    HotelSectionLabel(text = strings.selectedHotels)
                }

                items(state.selectedHotels, key = { "sel_${it.hotel.hotelKey}" }) { entry ->
                    SelectedHotelChip(
                        entry    = entry,
                        onRemove = { viewModel.onRemoveSelected(entry.hotel.hotelKey) }
                    )
                }

                item(key = "selected_divider") {
                    HorizontalDivider(
                        color    = Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            when (val s = state.searchState) {

                is SearchState.Idle -> item(key = "idle") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(strings.enterCityAndTap, color = Color.Gray, fontSize = 14.sp)
                    }
                }

                is SearchState.Loading -> item(key = "loading") {
                    HotelLoadingState(message = strings.searchingHotels)
                }

                is SearchState.Error -> {
                    item(key = "error") {
                        SearchErrorCard(s.error, strings)
                    }
                }

                is SearchState.Success -> {
                    if (s.hotels.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(strings.noHotels, color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        if (state.selectedHotels.isNotEmpty()) {
                            item(key = "results_header") {
                                HotelSectionLabel(text = strings.searchResult)
                            }
                        }
                        items(s.hotels, key = { it.hotelKey }) { hotel ->
                            HotelOptionCard(
                                hotel              = hotel,
                                itemState          = state.itemStates[hotel.hotelKey] ?: HotelItemState(),
                                isAlreadySelected  = state.selectedHotels.any { it.hotel.hotelKey == hotel.hotelKey },
                                imageBitmap        = state.hotelImages[hotel.hotelKey],
                                onRequestImage     = { viewModel.loadHotelImage(hotel) },
                                onImageClick       = { bitmap -> fullScreenBitmap = bitmap },
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
private fun SelectedHotelChip(
    entry   : SelectedHotelEntry,
    onRemove: () -> Unit
) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SelectedChipBg)
            .border(1.dp, SelectedChipBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = entry.hotel.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = SelectedChipFg
            )
            Text(
                text     = "${entry.dateFrom} – ${entry.dateTo}  •  ${entry.days} ${strings.days}",
                fontSize = 12.sp,
                color    = Color(0xFF639922)
            )
            Text(
                text     = "${strings.total}: ${entry.totalCost} ₴",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color    = SelectedBorder
            )
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color.Transparent, RoundedCornerShape(6.dp))
                .border(0.5.dp, SelectedChipBorder, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onRemove, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = Icons.Filled.Close,
                    contentDescription = strings.remove,
                    tint               = SelectedChipFg,
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}
@Composable
private fun SearchErrorCard(
    error: SearchError,
    strings: AppStrings,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8E8E8))
            .border(
                0.5.dp,
                Color(0xFFE0E0E0),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    Color(0xFFFBEAEA),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB3261E)
            )
        }

        Spacer(Modifier.width(10.dp))

        if (error == SearchError.INVALID_REQUEST) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = error.toMessage(),
                    fontSize = 13.sp,
                    color = Color(0xFFB3261E)
                )

                Text(
                    text = strings.clarify,
                    fontSize = 13.sp,
                    color = Color(0xFFB3261E)
                )
            }
        } else {
            Text(
                text = error.toMessage(),
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = Color(0xFFB3261E)
            )
        }
    }
}
@Composable
private fun HotelOptionCard(
    hotel             : HotelResult,
    itemState         : HotelItemState,
    isAlreadySelected : Boolean,
    imageBitmap       : Bitmap?,
    onRequestImage    : () -> Unit,
    onImageClick      : (Bitmap) -> Unit,
    onToggleExpand    : () -> Unit,
    onDateFromSelected: (Long) -> Unit,
    onDateToSelected  : (Long) -> Unit,
    onAddClick        : () -> Unit
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    LaunchedEffect(hotel.hotelKey, hotel.imageUrl) {
        onRequestImage()
    }
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isAlreadySelected) SelectedCardBg else CardBackground)
            .border(
                width = if (isAlreadySelected) 1.5.dp else 0.5.dp,
                color = if (isAlreadySelected) SelectedBorder else CardBorder,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HotelThumbnail(
                    bitmap  = imageBitmap,
                    onClick = { imageBitmap?.let(onImageClick) }
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = hotel.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    if (hotel.address.isNotEmpty()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(text = hotel.address, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                if (isAlreadySelected) {
                    Box(
                        modifier = Modifier
                            .background(SelectedChipBg, RoundedCornerShape(20.dp))
                            .border(0.5.dp, SelectedChipBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = strings.selected,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SelectedChipFg
                        )
                    }
                }
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = strings.costPerDay, fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "${hotel.cost} ₴",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(ButtonBackground, RoundedCornerShape(8.dp))
                        .border(0.5.dp, CardBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onToggleExpand, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (itemState.isExpanded) Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (itemState.isExpanded) "Згорнути" else "Розгорнути",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = itemState.isExpanded,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isAlreadySelected) ExpandedSelectedBg else ExpandedBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(
                    color    = if (isAlreadySelected) SelectedChipBorder else CardBorder,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Text(
                    text       = strings.selectedDates,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.DarkGray
                )

                HotelDateRow(
                    label   = strings.fromHotelSearch,
                    value   = itemState.dateFrom,
                    onClick = { showFromPicker = true  }
                )
                HotelDateRow(
                    label   = strings.toHotelSearch,
                    value   = itemState.dateTo,
                    onClick = { showToPicker = true }
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text     = "${strings.duration}: ${if (itemState.days > 0) "${itemState.days} ${strings.days}" else "—"}",
                            fontSize = 12.sp,
                            color    = Color.Gray
                        )
                        Text(
                            text       = "${strings.totalCost}${itemState.days * hotel.cost} ₴",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                    if (itemState.days > 0) {
                        Button(
                            onClick = onAddClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAlreadySelected) AddGreenDark else AddGreen,
                                contentColor   = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier       = Modifier.height(36.dp)
                        ) {
                            Text(
                                text     = if (isAlreadySelected) strings.update else strings.addToTrip,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelDateRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = Color.DarkGray,
            modifier = Modifier.width(48.dp)
        )
        OutlinedButton(
            onClick        = onClick,
            modifier       = Modifier.fillMaxWidth().height(40.dp),
            shape          = RoundedCornerShape(8.dp),
            colors         = ButtonDefaults.outlinedButtonColors(
                containerColor = FieldBackground,
                contentColor   = Color.Black
            ),
            border         = BorderStroke(0.5.dp, CardBorder),
            contentPadding = PaddingValues(horizontal = 12.dp)
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

@Composable
private fun HotelSectionLabel(text: String) {
    Text(
        text          = text,
        fontSize      = 14.sp,
        fontWeight    = FontWeight.Medium,
        color         = Color.White,
        letterSpacing = 0.5.sp,
        modifier      = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}
@Composable
private fun HotelThumbnail(bitmap: Bitmap?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBorder)
            .clickable(enabled = bitmap != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap             = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector        = Icons.Default.Place,
                contentDescription = null,
                tint               = Color.Gray,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FullScreenPhotoDialog(bitmap: Bitmap, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null,
                modifier = Modifier.fillMaxWidth().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
                contentScale = ContentScale.FillWidth)
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape).clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Закрити", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun HotelLoadingState(message: String) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = Color.DarkGray)
        Text(text = message, color = Color.Gray, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss     : () -> Unit
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
private fun hotelTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    cursorColor             = Color.Black,
    focusedTextColor        = Color.Black,
    unfocusedTextColor      = Color.Black
)