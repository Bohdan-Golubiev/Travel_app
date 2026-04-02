package com.example.travelapp.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.travelapp.viewmodel.FindVehicleViewModel

data class BookingOption( //затичка
    val name: String,
    val time: String,
    val cost: String
)

val bookingOptions = listOf(
    BookingOption("Booking option", "time", "cost"),
    BookingOption("Booking option", "time", "cost"),
    BookingOption("Booking option", "time", "cost"),
    BookingOption("Booking option", "time", "cost"),
)
val transportOptions = listOf("Plane", "Train", "Bus", "Without")
private val CardBackground   = Color(0xFFCED4DA)
private val ButtonBackground = Color(0xFFD9D9D9)

@Composable
fun FindVehicleScreen(
    onNextClick: () -> Unit = {},
    viewModel: FindVehicleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Вибір транспорту
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transportOptions.forEach { option ->
                val isSelected = state.selectedTransport == option
                OutlinedButton(
                    onClick = { viewModel.onTransportSelected(option) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) ButtonBackground.copy(alpha = 0.6f) else ButtonBackground,
                        contentColor = Color.Black
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 0.dp,
                        color  = if (isSelected) Color.White else Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = option, fontSize = 13.sp, fontWeight = FontWeight.Normal)
                }
            }
        }

        OutlinedTextField(
            value = state.startPlace,
            onValueChange = viewModel::onStartPlaceChange,
            placeholder = { Text("Start place", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = vehicleTextFieldColors()
        )

        OutlinedTextField(
            value = state.endPlace,
            onValueChange = viewModel::onEndPlaceChange,
            placeholder = { Text("End place", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = vehicleTextFieldColors()
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
            ) { Text("Next →", fontSize = 16.sp) }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bookingOptions) { option ->
                BookingOptionItem(
                    option = option,
                    onAddClick = { viewModel.onAddClick(option) }
                )
            }
        }
    }
}

@Composable
private fun BookingOptionItem(
    option: BookingOption,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(10.dp))
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
            Text(text = option.cost, fontSize = 13.sp, color = Color.Black)
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ButtonBackground, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onAddClick, modifier = Modifier.fillMaxSize()) {
                Text(text = "+", fontSize = 22.sp, color = Color.Black)
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