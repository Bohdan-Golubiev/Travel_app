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
import com.example.travelapp.model.dataclasses.Review
import com.example.travelapp.model.dataclasses.ReviewPlace
import com.example.travelapp.model.dataclasses.ReviewService
import com.example.travelapp.ui.theme.TextPrimary
import com.example.travelapp.ui.theme.TextSecondary

val samplePlaces = listOf(
    ReviewPlace(
        id = 1,
        place = "Пам'ятник ...",
        text = "Чудово та цікаво",
        assessment = "9.5",
        createdAt = "01.03.2025"
    ),
    ReviewPlace(
        id = 2,
        place = "Річка ...",
        text = "Чудовий вид ...",
        assessment = "8.5",
        createdAt = "12.02.2026"
    ),
    ReviewPlace(
        id = 3,
            place = "Гора ...",
        text = "Дуже висока зі складним підйомом",
        assessment = "6.0",
        createdAt = "22.06.2025"
    )
)

val sampleServices = listOf(
    ReviewService(
        id = 1,
        service = "Готель «Централь»",
        text = "Чудовий готель з приємним персоналом та зручними номерами.",
        assessment = "9.2",
        createdAt = "12.03.2025"
    ),
    ReviewService(
        id = 2,
        service = "Ресторан «Смачно»",
        text = "Дуже смачна кухня, приємна атмосфера, рекомендую.",
        assessment = "8.5",
        createdAt = "01.02.2025"
    ),
    ReviewService(
        id = 3,
        service = "Spa «Релакс»",
        text = "Відмінний сервіс, розслаблююча обстановка, прийду ще.",
        assessment = "10.0",
        createdAt = "20.01.2025"
    )
)

@Composable
fun ReviewsScreen() {
    val allReviews: List<Review> = samplePlaces + sampleServices
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(allReviews) { review ->
            ReviewCard(review = review)
            HorizontalDivider(color = Color(0xFF2A4A5E))
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Відгук",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "Оцінка: ${review.assessment}",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = review.subject,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = review.createdAt,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = review.text,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp
        )
    }
}