package com.example.travelapp.view.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocalAppStrings
import com.example.travelapp.viewmodel.profile.MonthSpending
import com.example.travelapp.viewmodel.profile.SpendingStatsViewModel
import com.example.travelapp.viewmodel.profile.toLocalizedLabel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SpendingStatsScreen(
    userId: String,
    viewModel: SpendingStatsViewModel = viewModel(),
) {
    val stats by viewModel.stats.collectAsState()
    val strings = LocalAppStrings.current

    LaunchedEffect(userId) { viewModel.load(userId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCards(stats, strings)
        HorizontalDivider(color = Color(0xFF2A4A5E))
        if (stats.isNotEmpty()) {
            SpendingBarChart(stats, strings)
            HorizontalDivider(color = Color(0xFF2A4A5E))
            MonthBreakdownList(stats, strings)
        }
    }
}


@Composable
private fun SummaryCards(stats: List<MonthSpending>, strings: AppStrings) {
    val totalBooking = stats.sumOf { it.bookingCost }
    val totalHotel   = stats.sumOf { it.hotelCost }
    val total        = totalBooking + totalHotel

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label  = strings.flights,
                value  = totalBooking.formatCost(),
                accent = Color(0xFF378ADD),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label  = strings.hotels,
                value  = totalHotel.formatCost(),
                accent = Color(0xFF1D9E75),
                modifier = Modifier.weight(1f),
            )
        }
        StatCard(
            label     = strings.totalForMonths,
            value     = total.formatCost(),
            accent    = Color(0xFFBA7517),
            large     = true,
            modifier  = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF162032))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, color = Color(0xFF8EAABE))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text       = value,
            fontSize   = if (large) 26.sp else 20.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White,
        )
    }
}


private val colorBooking = Color(0xFF378ADD)
private val colorHotel   = Color(0xFF1D9E75)
private val colorTotal   = Color(0xFFBA7517)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SpendingBarChart(
    stats: List<MonthSpending>,
    strings: AppStrings
) {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(stats) { animated = true }

    val progress by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "chartAnim"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(color = colorBooking, label = strings.flights)
            LegendItem(color = colorHotel,   label = strings.hotels)
            LegendItem(color = colorTotal,   label = strings.total, dashed = true)
        }

        Spacer(Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val maxVal  = stats.maxOf { it.total }.takeIf { it > 0 } ?: 1.0
            val count   = stats.size
            val leftPad = 52f
            val botPad  = 32f
            val topPad  = 16f
            val chartW  = size.width - leftPad
            val chartH  = size.height - botPad - topPad

            val gridCount = 4
            repeat(gridCount + 1) { i ->
                val y = topPad + chartH - (chartH / gridCount) * i
                drawLine(
                    color       = Color(0xFF2A4A5E),
                    start       = Offset(leftPad, y),
                    end         = Offset(size.width, y),
                    strokeWidth = 0.5f,
                )
                val label = ((maxVal / gridCount) * i).formatAxis()
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    leftPad - 6f,
                    y + 4f,
                    android.graphics.Paint().apply {
                        color     = 0xFF8EAABE.toInt()
                        textSize  = 24f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // Bars
            val groupWidth = chartW / count
            val barWidth   = groupWidth * 0.28f
            val gap        = 4f

            stats.forEachIndexed { i, month ->
                val x0 = leftPad + groupWidth * i + groupWidth * 0.08f
                val bH = chartH * (month.bookingCost / maxVal).toFloat() * progress
                val hH = chartH * (month.hotelCost   / maxVal).toFloat() * progress

                if (bH > 0f) drawRoundRect(
                    color       = colorBooking,
                    topLeft     = Offset(x0, topPad + chartH - bH),
                    size        = Size(barWidth, bH),
                    cornerRadius = CornerRadius(4f, 4f),
                )
                if (hH > 0f) drawRoundRect(
                    color       = colorHotel,
                    topLeft     = Offset(x0 + barWidth + gap, topPad + chartH - hH),
                    size        = Size(barWidth, hH),
                    cornerRadius = CornerRadius(4f, 4f),
                )

                // X-axis
                drawContext.canvas.nativeCanvas.drawText(
                    month.yearMonth.toLocalizedLabel(strings.languageMonth),
                    x0 + barWidth,
                    size.height - 6f,
                    android.graphics.Paint().apply {
                        color     = 0xFF8EAABE.toInt()
                        textSize  = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            if (progress > 0f) {
                val points = stats.mapIndexed { i, month ->
                    val x0 = leftPad + (chartW / count) * i + (chartW / count) * 0.08f
                    val cx = x0 + barWidth + gap / 2f
                    val y  = topPad + chartH - chartH * (month.total / maxVal).toFloat() * progress
                    Offset(cx, y)
                }

                for (j in 0 until points.lastIndex) {
                    drawLine(
                        color       = colorTotal,
                        start       = points[j],
                        end         = points[j + 1],
                        strokeWidth = 3f,
                        cap         = StrokeCap.Round,
                        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(12f, 6f)),
                    )
                }
                points.forEach { pt ->
                    drawCircle(color = colorTotal, radius = 5f, center = pt)
                    drawCircle(color = Color(0xFF0D1B2A), radius = 2.5f, center = pt)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, dashed: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (dashed) {
            Canvas(modifier = Modifier.size(width = 18.dp, height = 10.dp)) {
                drawLine(
                    color       = color,
                    start       = Offset(0f, size.height / 2),
                    end         = Offset(size.width, size.height / 2),
                    strokeWidth = 3f,
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
                )
            }
        } else {
            Box(modifier = Modifier.size(width = 14.dp, height = 8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
        Spacer(Modifier.width(5.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF8EAABE))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthBreakdownList(
    stats: List<MonthSpending>,
    strings: AppStrings
) {
    val maxTotal = stats.maxOf { it.total }.takeIf { it > 0 } ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text      = strings.detailForMonths,
            fontSize  = 12.sp,
            color     = Color(0xFF8EAABE),
        )
        stats.reversed().forEach { month ->
            MonthRow(month = month, maxTotal = maxTotal, strings = strings)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthRow(
    month: MonthSpending,
    maxTotal: Double,
    strings: AppStrings
) {
    val pct = (month.total / maxTotal).toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = month.yearMonth.toLocalizedLabel(strings.languageMonth), fontSize = 13.sp, color = Color(0xFF8EAABE))
            Text(text = month.total.formatCost(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2A4A5E))
        ) {
            Row(modifier = Modifier.fillMaxHeight()) {
                if (month.bookingCost > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((month.bookingCost / maxTotal).toFloat().coerceAtLeast(0.001f))
                            .background(colorBooking)
                    )
                }
                if (month.hotelCost > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((month.hotelCost / maxTotal).toFloat().coerceAtLeast(0.001f))
                            .background(colorHotel)
                    )
                }
                val rest = 1f - pct
                if (rest > 0f) {
                    Box(modifier = Modifier.weight(rest.coerceAtLeast(0.001f)))
                }
            }
        }

        if (month.bookingCost > 0 || month.hotelCost > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (month.bookingCost > 0)
                    Text("✈ ${month.bookingCost.formatCost()}", fontSize = 11.sp, color = colorBooking)
                if (month.hotelCost > 0)
                    Text("🏨 ${month.hotelCost.formatCost()}", fontSize = 11.sp, color = colorHotel)
            }
        }
    }
}

private fun Double.formatCost(): String {
    return if (this >= 1_000) {
        "₴${"%,.0f".format(this)}"
    } else {
        "₴${"%.0f".format(this)}"
    }
}

private fun Double.formatAxis(): String =
    if (this >= 1_000) "${"%.0f".format(this / 1_000)}к" else "%.0f".format(this)