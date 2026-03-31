package com.example.travelapp.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.model.dataclasses.PaymentItem
import com.example.travelapp.ui.theme.TextPrimary
import com.example.travelapp.ui.theme.TextSecondary

val samplePayments = listOf(
    PaymentItem(
        id = 1,
        booking = "Сервіс ...",
        sum = "1 200 грн",
        billing = "Visa **** 4321",
        date = "15.03.2025"
    ),
    PaymentItem(
        id = 2,
        booking = "Квиток поїзду",
        sum = "850 грн",
        billing = "MasterCard **** 7890",
        date = "28.02.2025"
    ),
    PaymentItem(
        id = 3,
        booking = "Номер в готелі ...",
        sum = "2 400 грн",
        billing = "Visa **** 4321",
        date = "10.01.2025"
    )
)
@Composable
fun PaymentsScreen(
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(samplePayments, key = { it.id }) { payment ->
                PaymentCard(payment = payment)
                HorizontalDivider(color = Color(0xFF2A4A5E))
            }
        }
    }
}

@Composable
private fun PaymentCard(payment: PaymentItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = payment.booking,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = payment.sum,
            fontSize = 13.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = payment.billing,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = payment.date,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}