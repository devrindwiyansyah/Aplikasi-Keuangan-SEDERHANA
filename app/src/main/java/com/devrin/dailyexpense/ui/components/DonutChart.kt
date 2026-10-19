package com.devrin.dailyexpense.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devrin.dailyexpense.ui.theme.DarkText
import com.devrin.dailyexpense.ui.theme.GrayText

data class PieChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<PieChartSlice>,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 210.dp,
    strokeWidth: Dp = 28.dp
) {
    val total = remember(slices) { slices.sumOf { it.value } }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(chartSize)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.width - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            if (total <= 0.0 || slices.isEmpty()) {
                // Empty state ring
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.35f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx)
                )
            } else {
                val gapAngle = if (slices.size > 1) 2.5f else 0f
                val totalGap = gapAngle * slices.size
                val availableSweep = 360f - totalGap

                var currentStartAngle = -90f

                slices.forEach { slice ->
                    val fraction = (slice.value / total).toFloat()
                    val targetSweep = (fraction * availableSweep) * animationProgress.value

                    if (targetSweep > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = currentStartAngle,
                            sweepAngle = targetSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }

                    currentStartAngle += (fraction * availableSweep) + gapAngle
                }
            }
        }

        // Center label & total amount
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = centerTitle,
                style = MaterialTheme.typography.bodySmall,
                color = GrayText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = centerValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                textAlign = TextAlign.Center
            )
        }
    }
}
