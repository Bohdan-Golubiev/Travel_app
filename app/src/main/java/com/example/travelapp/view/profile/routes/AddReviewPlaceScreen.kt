package com.example.travelapp.view.profile.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.data.entity.PlaceEntity
import com.example.travelapp.viewmodel.profile.AddReviewPlaceViewModel

@Composable
fun AddReviewPlaceScreen(
    place: PlaceEntity,
    userId: String,
    viewModel: AddReviewPlaceViewModel = viewModel(),
    onSubmit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onSubmit()
    }

    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            },
            text = { Text(message, color = Color.White) },
            containerColor = Color(0xFF1A3447)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.location,
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "Order in route: ${place.orderInRoute + 1}",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }

            HorizontalDivider(color = Color(0xFF2A4A5E))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Rating",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        IconButton(
                            onClick = { viewModel.onRatingSelected(star) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= uiState.selectedRating)
                                    Icons.Filled.Star
                                else
                                    Icons.Outlined.Star,
                                contentDescription = "Star $star",
                                tint = if (star <= uiState.selectedRating)
                                    Color(0xFF219EBC)
                                else
                                    Color(0xFF5E7A8A),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    if (uiState.selectedRating > 0) {
                        Text(
                            text = "${uiState.selectedRating} / 5",
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF2A4A5E))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Comment",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                OutlinedTextField(
                    value = uiState.commentText,
                    onValueChange = viewModel::onCommentChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = {
                        Text(
                            text = "Write your review here...",
                            fontSize = 14.sp,
                            color = Color(0xFF5E7A8A)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF219EBC),
                        unfocusedBorderColor = Color(0xFF2A4A5E),
                        cursorColor = Color(0xFF219EBC)
                    ),
                    supportingText = {
                        Text(
                            text = "${uiState.commentText.length}/1000",
                            fontSize = 12.sp,
                            color = Color(0xFF5E7A8A)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 8
                )
            }
        }

        Button(
            onClick = { viewModel.submitReview(userId, place) },
            enabled = uiState.isSubmitEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF219EBC),
                disabledContainerColor = Color(0xFF2A4A5E)
            )
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Submit", fontSize = 15.sp)
            }
        }
    }
}