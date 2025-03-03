package com.kaleyra.video_sdk.call.stream.view.audio

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlin.random.Random

internal val AudioVisualizerHeightAnimationDuration = 80
internal val AudioVisualizerEnableAnimationDuration = 500

@Composable
internal fun AudioVisualizer(
    modifier: Modifier = Modifier,
    barWidth: Dp,
    barSpacing: Dp,
    barCount: Int,
    enable: Boolean = true,
    color: Color = LocalContentColor.current
) {
    val random = remember { Random(System.currentTimeMillis()) }
    var reset by remember { mutableStateOf(false) }

    LaunchedEffect(reset) {
        delay(100)
        reset = !reset
    }

    val animatedHeights = mutableListOf<State<Float>>()
    val heights = remember(reset) {
        mutableListOf<Float>().apply {
            val seed = random.nextFloatInRange(0.3f, 0.7f)
            repeat(barCount) { index ->
                val multiplier  = if (index % 2 == 0) random.nextFloatInRange(1.25f, 1.4f) else random.nextFloatInRange(2f, 2.2f)
                this += (seed * multiplier).coerceAtMost(1f)
            }
        }
    }
    val heightMultiplier by animateFloatAsState(
        targetValue = if (enable) 1f else 0f,
        animationSpec = tween(AudioVisualizerEnableAnimationDuration, easing = LinearEasing)
    )

    repeat(barCount) { index ->
        animatedHeights += animateFloatAsState(
            targetValue = heights[index],
            animationSpec = tween(durationMillis = AudioVisualizerHeightAnimationDuration)
        )
    }

    Canvas(modifier = modifier) {
        val barWidthFloat = barWidth.toPx()
        val barSpacingFloat = barSpacing.toPx()
        val count = (size.width / (barWidthFloat + barSpacingFloat)).toInt().coerceAtMost(barCount)

        val animatedVolumeWidth = barWidthFloat * count + barSpacingFloat * (count - 1)
        var startOffset = (size.width - animatedVolumeWidth) / 2 + barWidthFloat / 2

        val barMaxHeight = (size.height / 2f) * heightMultiplier

        repeat(count) { index ->
            val barHeight = animatedHeights[index].value * barMaxHeight
            drawLine(
                color = color,
                start = Offset(startOffset, center.y - barHeight / 2),
                end = Offset(startOffset, center.y + barHeight / 2),
                strokeWidth = barWidthFloat,
                cap = StrokeCap.Round
            )
            startOffset += barWidthFloat + barSpacingFloat
        }
    }
}

private fun Random.nextFloatInRange(min: Float, max: Float): Float {
    require(min < max) { "min must be less than max" }
    return nextFloat() * (max - min) + min
}