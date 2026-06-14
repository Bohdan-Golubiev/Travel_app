package com.example.travelapp.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.utils.LocalAppStrings

private val AccentBlue      = Color(0xFF219EBC)
private val AccentBlueLight = Color(0xFF52D9F5)
private val SurfaceDim      = Color(0x10FFFFFF)
private val SurfaceWish     = Color(0x14219EBC)
private val BorderWish      = Color(0x33219EBC)
private val BorderCard      = Color(0x1FFFFFFF)
private val TextMuted       = Color(0x73FFFFFF)
private val TextSub         = Color(0xBFFFFFFF)

@Composable
fun RouteCreatedScreen(
    routeName: String,
    onMakeBooking: () -> Unit,
    onDoneClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Row(
            modifier            = Modifier
                .size(96.dp)
                .background(SurfaceWish, CircleShape),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "🗺️",
                fontSize   = 64.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text       = strings.createdSuccessful,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = strings.savedSuccessful,
            fontSize  = 13.sp,
            color     = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDim, RoundedCornerShape(14.dp))
                .border(0.5.dp, BorderCard, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text     = strings.route.uppercase(),
                fontSize = 11.sp,
                color    = TextMuted,
                letterSpacing = 1.2.sp
            )
            Text(
                text       = routeName,
                fontSize   = 17.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWish, RoundedCornerShape(12.dp))
                .border(0.5.dp, BorderWish, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.wishes,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlueLight
                )
                Text(
                    text = strings.wishesRoute,
                    fontSize = 12.sp,
                    color = TextSub,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onMakeBooking,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = strings.makeBooking,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentBlue.copy(alpha = 0.5f))
        ) {
            Text(
                text = strings.toRoutes,
                fontSize = 16.sp,
                color = AccentBlueLight
            )
        }
    }
}