package com.kaleyra.video_sdk.call.screen.view

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.intermediateLayout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeWidth
import kotlinx.coroutines.launch
import kotlin.math.ceil

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreamGrid(
    maxWidth: Dp,
    thumbnailsSize: Dp,
    streams: @Composable LookaheadScope.() -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val thumbnailSizePx = with(density) { thumbnailsSize.toPx().toInt() }
    val isPortrait by remember {
        derivedStateOf {
            configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        }
    }

    LookaheadScope {
        Layout(
            content = { streams(this) }
        ) { measurables, constraints ->
            check(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
                "unbounded size not supported"
            }

            val thereIsAPinnedElement = measurables.any { (it.parentData as StreamParentData).pin }

            if (!thereIsAPinnedElement) {
                val (rows, columns) = calculateRowsAndColumns(
                    isPortrait,
                    maxWidth,
                    measurables.size
                )

                val itemWidth = constraints.maxWidth / columns
                val itemHeight = if (rows != 0) constraints.maxHeight / rows else 0
                val itemConstraints = constraints.copy(maxWidth = itemWidth, maxHeight = itemHeight)

                val lastRowItemsCount =
                    if (rows != 0) measurables.size - (columns * (rows - 1)) else 0
                val lastRowPadding = (constraints.maxWidth - (lastRowItemsCount * itemWidth)) / 2

                val placeables = measurables.map { measurable ->
                    measurable.measure(itemConstraints)
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    var xPosition = 0
                    var yPosition = 0
                    var currentRow = 0

                    placeables.forEachIndexed { index, placeable ->
                        placeable.place(x = xPosition, y = yPosition)

                        if (index % columns == columns - 1) {
                            xPosition = 0
                            yPosition += itemHeight
                            currentRow += 1
                        } else {
                            xPosition += itemWidth
                        }

                        if (xPosition == 0 && currentRow == rows - 1) {
                            xPosition += lastRowPadding
                        }
                    }
                }
            } else {
                val thumbnailsPosition = when {
                    isPortrait -> 0 // bottom
                    maxWidth >= 900.dp -> 1 // left
                    else -> 2 // right
                }
                val pinnedItemsCount = measurables.count { (it.parentData as StreamParentData).pin }
                val (rows, columns) = calculateRowsAndColumns(
                    isPortrait,
                    maxWidth,
                    pinnedItemsCount
                )

                val itemWidth = when (thumbnailsPosition) {
                    0 -> constraints.maxWidth / columns
                    else -> (constraints.maxWidth - thumbnailSizePx) / columns
                }
                val itemHeight = when (thumbnailsPosition) {
                    0 -> if (rows != 0) (constraints.maxHeight - thumbnailSizePx) / rows else 0
                    else -> if (rows != 0) constraints.maxHeight / rows else 0
                }

                val itemConstraints = constraints.copy(maxWidth = itemWidth, maxHeight = itemHeight)

                val lastRowItemsCount =
                    if (rows != 0) pinnedItemsCount - (columns * (rows - 1)) else 0
                val lastRowPadding = when (thumbnailsPosition) {
                    0 -> (constraints.maxWidth - (lastRowItemsCount * itemWidth)) / 2
                    else -> ((constraints.maxWidth - thumbnailSizePx) - (lastRowItemsCount * itemWidth)) / 2
                }

                val thumbnailConstraints = Constraints.fixed(thumbnailSizePx, thumbnailSizePx)

                val pinned = mutableListOf<Placeable>()
                val thumbnails = mutableListOf<Placeable>()

                measurables.map { measurable ->
                    if ((measurable.parentData as StreamParentData).pin) {
                        pinned.add(measurable.measure(itemConstraints))
                    } else {
                        thumbnails.add(measurable.measure(thumbnailConstraints))
                    }
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val leftMargin = if (thumbnailsPosition == 1) thumbnailSizePx else 0
                    var xPosition = leftMargin
                    var yPosition = 0
                    var currentRow = 0

                    pinned.forEachIndexed { index, placeable ->
                        placeable.place(x = xPosition, y = yPosition)

                        if (index % columns == columns - 1) {
                            xPosition = leftMargin
                            yPosition += itemHeight
                            currentRow += 1
                        } else {
                            xPosition += itemWidth
                        }

                        if (xPosition == leftMargin && currentRow == rows - 1) {
                            xPosition += lastRowPadding
                        }
                    }

                    when (thumbnailsPosition) {
                        0 -> {
                            val padding =
                                (constraints.maxWidth - (thumbnails.size.coerceAtMost(3) * thumbnailSizePx)) / 2
                            thumbnails
                                .take(3)
                                .forEachIndexed { index, placeable ->
                                    placeable.place(
                                        x = padding + thumbnailSizePx * index,
                                        y = constraints.maxHeight - thumbnailSizePx
                                    )
                                }
                        }

                        1 -> {
                            val padding =
                                (constraints.maxHeight - (thumbnails.size.coerceAtMost(3) * thumbnailSizePx)) / 2
                            thumbnails
                                .take(3)
                                .forEachIndexed { index, placeable ->
                                    placeable.place(
                                        x = 0,
                                        y = padding + thumbnailSizePx * index
                                    )
                                }
                        }

                        2 -> {
                            val padding =
                                (constraints.maxHeight - (thumbnails.size.coerceAtMost(3) * thumbnailSizePx)) / 2
                            thumbnails
                                .take(3)
                                .forEachIndexed { index, placeable ->
                                    placeable.place(
                                        x = constraints.maxWidth - thumbnailSizePx,
                                        y = padding + thumbnailSizePx * index
                                    )
                                }
                        }
                    }
                }
            }
        }

    }
}

fun calculateRowsAndColumns(isPortrait: Boolean, maxWidth: Dp, itemsCount: Int): Pair<Int, Int> {
    val columns: Int
    val rows: Int
    val isAtLeastMediumSizeWidth = maxWidth.isAtLeastMediumSizeWidth()
    when {
        isPortrait -> {
            columns = when {
                isAtLeastMediumSizeWidth && itemsCount < 3 -> 1
                isAtLeastMediumSizeWidth && itemsCount < 5 -> 2
                itemsCount < 4 -> 1
                itemsCount == 4 -> 2
                else -> ceil(itemsCount / 4f).toInt()
            }
            rows = ceil(itemsCount / columns.toFloat()).toInt()
        }

        else -> {
            rows =  when {
                itemsCount < 4 -> 1
                itemsCount == 4 -> 2
                else -> ceil(itemsCount / 4f).toInt()
            }
            columns = ceil(itemsCount / rows.toFloat()).toInt()
        }
    }
    return rows to columns
}


@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.animateConstraints(lookaheadScope: LookaheadScope) = composed {
    var sizeAnimation: Animatable<IntSize, AnimationVector2D>? by remember { mutableStateOf(null) }
    var targetSize: IntSize? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        snapshotFlow { targetSize }.collect { target ->
            if (target != null && target != sizeAnimation?.targetValue) {
                sizeAnimation?.run {
                    launch { animateTo(target, tween(durationMillis = 100)) }
                } ?: Animatable(target, IntSize.VectorConverter).let {
                    sizeAnimation = it
                }
            }
        }
    }

    with(lookaheadScope) {
        this@composed.intermediateLayout { measurable, _ ->
            targetSize = lookaheadSize
            val (width, height) = sizeAnimation?.value ?: lookaheadSize
            val animatedConstraints = Constraints.fixed(width, height)

            val placeable = measurable.measure(animatedConstraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }
}

fun Modifier.animatePlacement(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animatable by remember {
        mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
    }
    this
        .onPlaced {
            targetOffset = it
                .positionInParent()
                .round()
        }
        .offset {
            val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter)
                .also { animatable = it }
            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(targetOffset, spring(stiffness = StiffnessMediumLow))
                }
            }
            animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
        }
}

@LayoutScopeMarker
@Immutable
object StreamGridScope {

    @Stable
    fun Modifier.pinMode(
        pin: Boolean
    ): Modifier {
        return then(StreamParentData(pin = pin))
    }
}

class StreamParentData(
    val pin: Boolean
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@StreamParentData
}