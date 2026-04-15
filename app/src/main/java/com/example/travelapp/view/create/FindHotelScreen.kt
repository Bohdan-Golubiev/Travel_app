package com.example.travelapp.view.create

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.travelapp.viewmodel.create.FindHotelViewModel
import com.example.travelapp.viewmodel.create.HotelItemState
import com.example.travelapp.viewmodel.create.hotelOptions

private val CardBackground   = Color(0xFFCED4DA)
private val ButtonBackground = Color(0xFFD9D9D9)
private val FieldBackground  = Color(0xFFE8E8E8)

data class HotelOption(
    val name: String,
    val costPerDay: String,
    val costPerDayValue: Int
)
@Composable
fun FindHotelScreen(
    onNextClick: () -> Unit = {},
    viewModel: FindHotelViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = state.startPlace,
            onValueChange = viewModel::onStartPlaceChange,
            placeholder = { Text("Start place", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = hotelTextFieldColors()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text("Search", fontSize = 16.sp) }

            Button(
                onClick = onNextClick,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text("Next ->", fontSize = 16.sp) }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(hotelOptions) { index, option ->
                HotelOptionItem(
                    option = option,
                    itemState = state.itemStates[index],
                    onToggleExpand = { viewModel.toggleExpand(index) },
                    onDateFromSelected = { millis -> viewModel.onDateFromSelected(index, millis) },
                    onDateToSelected = { millis -> viewModel.onDateToSelected(index, millis) },
                    onAddClick = { viewModel.onAddClick(index) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HotelOptionItem(
    option: HotelOption,
    itemState: HotelItemState,
    onToggleExpand: () -> Unit,
    onDateFromSelected: (Long) -> Unit,
    onDateToSelected: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }

    val total = if (itemState.days > 0) (itemState.days * option.costPerDayValue).toString() else "000"

    if (showFromPicker) {
        DatePickerModal(
            onDateSelected = { it?.let(onDateFromSelected) },
            onDismiss = { showFromPicker = false }
        )
    }
    if (showToPicker) {
        DatePickerModal(
            onDateSelected = { it?.let(onDateToSelected) },
            onDismiss = { showToPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = option.name, fontSize = 15.sp, color = Color.Black, modifier = Modifier.weight(1f))
            SmallSquareButton(label = "+", onClick = onAddClick)
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${option.costPerDay} ${option.costPerDayValue}",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            SmallSquareButton(
                label = if (itemState.isExpanded) "∧" else "V",
                onClick = onToggleExpand
            )
        }

        AnimatedVisibility(
            visible = itemState.isExpanded,
            enter = fadeIn() + expandVertically(),
            exit  = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.4f))

                Text("Select date", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)

                DateRow(label = "From:", value = itemState.dateFrom, onClick = { showFromPicker = true })
                DateRow(label = "To:",   value = itemState.dateTo,   onClick = { showToPicker = true })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total sum for ${if (itemState.days > 0) itemState.days else "N"} days:",
                        fontSize = 13.sp, color = Color.Black
                    )
                    Text(text = total, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Medium)
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
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = FieldBackground,
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
private fun SmallSquareButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(ButtonBackground, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Text(text = label, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }
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