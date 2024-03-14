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

@Immutable
enum class ThumbnailsPosition {
    Top, Left, Bottom, Right;

    fun isHorizontal() = this == Left || this == Right

    fun isVertical() = this == Top || this == Bottom
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreamGrid(
    maxWidth: Dp,
    thumbnailsSize: Dp,
    thumbnailsPosition: ThumbnailsPosition,
    thumbnailsCount: Int,
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

            val pinnedItemsCount = measurables.count { (it.parentData as StreamParentData).pin }
            val featuredItemsCount = pinnedItemsCount.takeIf { it != 0 } ?: measurables.size
            val thumbnailsPosition = thumbnailsPosition.takeIf { pinnedItemsCount > 0 }

            val (rows, columns) = calculateRowsAndColumns(isPortrait, maxWidth, featuredItemsCount)

            val featuredItemWidth = calculateFeaturedItemsWidth(thumbnailsPosition, thumbnailSizePx, columns,constraints)
            val featuredItemHeight = calculateFeaturedItemsHeight(thumbnailsPosition, thumbnailSizePx, rows, constraints)

            val featuredItemConstraints = constraints.copy(maxWidth = featuredItemWidth, maxHeight = featuredItemHeight)

            val thumbnailsPadding = if (thumbnailsPosition?.isHorizontal() == false) thumbnailSizePx else 0
            val featuredItemsPadding = calculateLastRowFeaturedPadding(rows, columns, featuredItemsCount, featuredItemWidth, thumbnailsPadding, constraints)

            val (featuredItems, thumbnailItems) = if (pinnedItemsCount == 0) {
                measurables.map { it.measure(featuredItemConstraints) } to emptyList<Placeable>()
            } else {
                val thumbnailConstraints = Constraints.fixed(thumbnailSizePx, thumbnailSizePx)

                val featuredItems = mutableListOf<Placeable>()
                val thumbnailItems = mutableListOf<Placeable>()

                measurables.map { measurable ->
                    if ((measurable.parentData as StreamParentData).pin) {
                        featuredItems.add(measurable.measure(featuredItemConstraints))
                    } else {
                        thumbnailItems.add(measurable.measure(thumbnailConstraints))
                    }
                }
                featuredItems to thumbnailItems
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                val startX = when (thumbnailsPosition) {
                    null -> 0
                    ThumbnailsPosition.Left -> thumbnailSizePx
                    else -> 0
                }
                val startY = when (thumbnailsPosition) {
                    null -> 0
                    ThumbnailsPosition.Top -> thumbnailSizePx
                    else -> 0
                }
                var x = startX
                var y = startY
                var nRow = 0

                featuredItems.forEachIndexed { index, placeable ->
                    placeable.place(x, y)

                    if (index % columns == columns - 1) {
                        x = startX
                        y += featuredItemHeight
                        nRow += 1
                    } else {
                        x += featuredItemWidth
                    }

                    if (x == startX && nRow == rows - 1) {
                        x += featuredItemsPadding
                    }
                }

                if (thumbnailsPosition != null && thumbnailItems.isNotEmpty()) {
                    placeThumbnailItems(
                        thumbnailItems.take(thumbnailsCount),
                        thumbnailSizePx,
                        thumbnailsPosition,
                        constraints
                    )
                }
            }
        }
    }
}

private fun calculateLastRowFeaturedPadding(
    rows: Int,
    columns: Int,
    featuredItemsCount: Int,
    featuredItemWidth: Int,
    thumbnailsPadding: Int,
    constraints: Constraints
) : Int {
    val lastRowFeaturedItemsCount = featuredItemsCount - (columns * (rows - 1))
    return (constraints.maxWidth - thumbnailsPadding - (lastRowFeaturedItemsCount * featuredItemWidth)) / 2
}

private fun calculateFeaturedItemsWidth(
    thumbnailsPosition: ThumbnailsPosition?,
    thumbnailSize: Int,
    columns: Int,
    constraints: Constraints
): Int {
    return if (thumbnailsPosition == null || thumbnailsPosition.isVertical()) constraints.maxWidth / columns
    else (constraints.maxWidth - thumbnailSize) / columns
}

private fun calculateFeaturedItemsHeight(
    thumbnailsPosition: ThumbnailsPosition?,
    thumbnailSize: Int,
    rows: Int,
    constraints: Constraints
): Int {
    return if (thumbnailsPosition == null || thumbnailsPosition.isHorizontal()) constraints.maxHeight / rows
    else (constraints.maxHeight - thumbnailSize) / rows
}

private fun Placeable.PlacementScope.placeThumbnailItems(
    items: List<Placeable>,
    thumbnailsSize: Int,
    thumbnailsPosition: ThumbnailsPosition,
    constraints: Constraints
) {
    val startX = when (thumbnailsPosition) {
        ThumbnailsPosition.Top, ThumbnailsPosition.Bottom -> (constraints.maxWidth - items.size * thumbnailsSize) / 2
        ThumbnailsPosition.Right -> constraints.maxWidth - thumbnailsSize
        ThumbnailsPosition.Left -> 0
    }
    val startY = when (thumbnailsPosition) {
        ThumbnailsPosition.Left, ThumbnailsPosition.Right -> (constraints.maxHeight - items.size * thumbnailsSize) / 2
        ThumbnailsPosition.Bottom -> constraints.maxHeight - thumbnailsSize
        ThumbnailsPosition.Top -> 0
    }

    if (thumbnailsPosition.isVertical()) {
        items.forEachIndexed { i, p -> p.place(x = startX + thumbnailsSize * i, y = startY) }
    } else {
        items.forEachIndexed { i, p -> p.place(x = startX, y = startY + thumbnailsSize * i) }
    }
}

fun calculateRowsAndColumns(isPortrait: Boolean, maxWidth: Dp, itemsCount: Int): Pair<Int, Int> {
    val isAtLeastMediumSizeWidth = maxWidth.isAtLeastMediumSizeWidth()
    val columns: Int
    val rows: Int
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
            rows = when {
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
fun Modifier.animateConstraints() = composed {
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