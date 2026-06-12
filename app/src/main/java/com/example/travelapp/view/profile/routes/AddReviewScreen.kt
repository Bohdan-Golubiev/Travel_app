package com.example.travelapp.view.profile.routes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.model.ReviewTarget
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.review.AddReviewViewModel

private val AccentBlue   = Color(0xFF219EBC)
private val AccentTeal   = Color(0xFF0D7FAE)
private val CardBg       = Color(0xFF1A3550)
private val CardBgLight  = Color(0xFF1E3D5C)
private val DividerColor = Color(0xFF2A4A5E)
private val TextMuted    = Color(0xFF90A4AE)
private val TextSub      = Color(0xFFB0BEC5)
private val StarOff      = Color(0xFF2A4A5E)

private val ratingColors = listOf(
    Color.Transparent,
    Color(0xFFEF5350),
    Color(0xFFFFA726),
    Color(0xFFFFD600),
    Color(0xFF66BB6A),
    Color(0xFF28B8DC)
)
private data class TargetMeta(val icon: String, val tint: Color)
private fun ReviewTarget.meta() = when (this) {
    is ReviewTarget.Place   -> TargetMeta("📍", Color(0xFF219EBC))
    is ReviewTarget.Hotel   -> TargetMeta("🏨", Color(0xFF66BB6A))
    is ReviewTarget.Booking -> TargetMeta("✈", Color(0xFFFFFFFF))
}
@Composable
fun AddReviewScreen(
    target: ReviewTarget,
    userId: String,
    viewModel: AddReviewViewModel = viewModel(),
    onSubmit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onSubmit()
    }

    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK", color = AccentBlue) }
            },
            text = { Text(message, color = Color.White) },
            containerColor = CardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            TargetHeader(target = target, strings = strings)

            Spacer(Modifier.height(16.dp))

            RatingSection(
                selectedRating = uiState.selectedRating,
                onRatingSelected = viewModel::onRatingSelected,
                strings = strings
            )

            Spacer(Modifier.height(16.dp))

            CommentSection(
                commentText = uiState.commentText,
                onCommentChanged = viewModel::onCommentChanged,
                strings = strings
            )

            Spacer(Modifier.height(8.dp))
        }

        SubmitButton(
            isSubmitting = uiState.isSubmitting,
            isEnabled = uiState.isSubmitEnabled,
            label = strings.submit,
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = { viewModel.submitReview(userId, target) }
        )
    }
}

@Composable
private fun TargetHeader(target: ReviewTarget, strings: AppStrings) {
    val meta = target.meta()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(meta.tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = meta.icon,
                    fontSize = 30.sp,
                    color = meta.tint
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = target.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (target is ReviewTarget.Booking) {
                    Text(
                        text = strings.flyFrom + target.from + strings.flyTo + target.to,
                        fontSize = 13.sp,
                        color = TextSub
                    )
                }
                Text(
                    text = target.subtitle,
                    fontSize = 13.sp,
                    color = TextMuted
                )
                if (target is ReviewTarget.Place) {
                    Text(
                        text = strings.orderInRoute + "${target.entity.orderInRoute + 1}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingSection(
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit,
    strings: AppStrings
) {
    val ratingLabels = listOf(
        "",
        strings.ratingBad,
        strings.ratingSoSo,
        strings.ratingOk,
        strings.ratingGood,
        strings.ratingExcellent
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val labelColor by animateColorAsState(
            targetValue = if (selectedRating > 0) ratingColors[selectedRating] else TextMuted,
            animationSpec = tween(300),
            label = "labelColor"
        )
        Text(
            text = if (selectedRating > 0) ratingLabels[selectedRating] else strings.ratingPrompt,
            fontSize = 14.sp,
            fontWeight = if (selectedRating > 0) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (star in 1..5) {
                val isSelected = star <= selectedRating
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                    label = "starScale$star"
                )
                val starColor by animateColorAsState(
                    targetValue = if (isSelected) ratingColors[selectedRating] else StarOff,
                    animationSpec = tween(200),
                    label = "starColor$star"
                )
                IconButton(
                    onClick = { onRatingSelected(star) },
                    modifier = Modifier
                        .size(48.dp)
                        .scale(scale)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Star $star",
                        tint = starColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (dot in 1..5) {
                val filled = dot <= selectedRating
                val dotColor by animateColorAsState(
                    targetValue = if (filled) ratingColors[selectedRating] else StarOff,
                    animationSpec = tween(200),
                    label = "dot$dot"
                )
                Box(
                    modifier = Modifier
                        .size(if (filled) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun CommentSection(
    commentText: String,
    onCommentChanged: (String) -> Unit,
    strings: AppStrings
) {
    val charCount = commentText.length
    val maxChars = 1000
    val progress = charCount / maxChars.toFloat()

    val progressColor = when {
        progress > 0.9f -> Color(0xFFEF5350)
        progress > 0.7f -> Color(0xFFFFA726)
        else            -> AccentBlue
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = strings.comment,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 130.dp),
            placeholder = {
                Text(
                    text = strings.writeReview,
                    fontSize = 14.sp,
                    color = Color(0xFF729EB9)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerColor,
                focusedContainerColor = CardBgLight,
                unfocusedContainerColor = CardBg,
                cursorColor = AccentBlue
            ),
            shape = RoundedCornerShape(14.dp)
        )
        Text(
            text = "$charCount / $maxChars",
            fontSize = 12.sp,
            color = progressColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SubmitButton(
    isSubmitting: Boolean,
    isEnabled: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xE6102030))
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = DividerColor
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isEnabled)
                            Modifier.background(
                                Brush.horizontalGradient(listOf(AccentBlue, AccentTeal))
                            )
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) Color.White else TextMuted
                    )
                }
            }
        }
    }
}