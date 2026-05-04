package com.example.travelapp.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.viewmodel.create.SelectedHotelEntry

private val CardBg    = Color(0xFFCED4DA)
private val GreenOk   = Color(0xFF388E3C)
private val BtnColor  = Color(0xFFD9D9D9)

@Composable
fun HotelBookedScreen(
    selectedHotels   : List<SelectedHotelEntry>,
    selectedVehicles : List<BookingOption>,
    onDoneClick      : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        LazyColumn(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    imageVector        = Icons.Filled.CheckCircle,
                    contentDescription = "Done",
                    tint               = GreenOk,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .size(64.dp)
                )
            }
            item {
                Text(
                    text       = "Services booked!",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    text      = "Your selections have been saved successfully.",
                    fontSize  = 14.sp,
                    color     = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
            item {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
            }

            if (selectedVehicles.isNotEmpty()) {
                item {
                    Text(
                        text       = "Your transport",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }
                items(selectedVehicles) { vehicle ->
                    BookedVehicleCard(vehicle)
                }
            }

            if (selectedHotels.isNotEmpty()) {
                item {
                    Text(
                        text       = "Your hotels",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }
                items(selectedHotels, key = { it.hotel.hotelKey }) { entry ->
                    BookedHotelCard(entry)
                }
            }

            if (selectedHotels.isEmpty() && selectedVehicles.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No services selected.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick  = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BtnColor,
                contentColor   = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("Done", fontSize = 16.sp)
        }
    }
}

@Composable
private fun BookedHotelCard(entry: SelectedHotelEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text       = entry.hotel.name,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color.Black
        )

        if (entry.hotel.address.isNotEmpty()) {
            Text(
                text     = entry.hotel.address,
                fontSize = 12.sp,
                color    = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                InfoRow(label = "From:", value = entry.dateFrom)
                InfoRow(label = "To:",   value = entry.dateTo)
                InfoRow(label = "Days:", value = "${entry.days}")
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "${entry.totalCost} $",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GreenOk
                )
                Text(
                    text     = "total",
                    fontSize = 11.sp,
                    color    = Color.Gray
                )
            }
        }
    }
}
@Composable
private fun BookedVehicleCard(vehicle: BookingOption) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text       = vehicle.name,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color.Black
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                InfoRow(label = "From:", value = vehicle.from)
                InfoRow(label = "To:",   value = vehicle.to)
                InfoRow(label = "Date:", value = vehicle.date)
                InfoRow(label = "Time:", value = vehicle.time)
            }
            Text(
                text       = "100 $",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = GreenOk
            )
        }
    }
}
@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, color = Color.Black)
    }
}