package com.example.travelapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.data.entity.ReviewEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ReviewItem(
    review: ReviewEntity,
    currentUserId: String,
    youLabel: String,
) {
    val formattedDate = remember(review.createdAt) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(review.createdAt))
    }

    val isOwn = review.userId == currentUserId
    val displayName = if (isOwn) youLabel else review.userName

    val mark = review.mark.coerceIn(0, 5)
    val starsFilled = mark
    val starsEmpty = 5 - mark

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF132D3E),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(starsFilled) {
                        Text(text = "★", fontSize = 16.sp, color = Color(0xFFFFCA28))
                    }
                    repeat(starsEmpty) {
                        Text(text = "★", fontSize = 16.sp, color = Color(0xFF2A4A5E))
                    }
                }
            }

            if (review.text.isNotBlank()) {
                Text(
                    text = review.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFCFD8DC),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color(0xFF5E7A8A)
                )
            }
        }
    }
}