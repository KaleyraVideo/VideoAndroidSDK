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
import androidx.compose.ui.layout.Measurable
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

@Composable
fun StreamGrid(
    maxWidth: Dp,
    thumbnailSize: Dp,
    thumbnailsPosition: ThumbnailsPosition,
    thumbnailsCount: Int,
    streams: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val thumbnailSizePx = with(density) { thumbnailSize.toPx().toInt() }
    val isPortrait by remember {
        derivedStateOf {
            configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        }
    }

    Layout(
        content = streams
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            "unbounded size not supported"
        }

        val pinnedItemsCount = measurables.count { (it.parentData as StreamParentData).pin }
        val featuredItemsCount = pinnedItemsCount.takeIf { it != 0 } ?: measurables.size
        val thumbnailsPosition = thumbnailsPosition.takeIf { pinnedItemsCount > 0 }

        val (rows, columns) = calculateRowsAndColumns(isPortrait, maxWidth, featuredItemsCount)

        val featuredItemWidth = calculateFeaturedItemsWidth(thumbnailsPosition, thumbnailSizePx, columns, constraints)
        val featuredItemHeight = calculateFeaturedItemsHeight(thumbnailsPosition, thumbnailSizePx, rows, constraints)

        val featuredItemConstraints = constraints.copy(maxWidth = featuredItemWidth, maxHeight = featuredItemHeight)
        val thumbnailItemConstraints = Constraints.fixed(thumbnailSizePx, thumbnailSizePx)

        val (featuredItems, thumbnailItems) = measure(measurables, featuredItemConstraints, thumbnailItemConstraints)

        val thumbnailsPadding = if (thumbnailsPosition?.isHorizontal() == true) thumbnailSizePx else 0
        val featuredItemsPadding = calculateFeaturedItemsPadding(
            rows = rows,
            columns = columns,
            featuredItemsCount = featuredItemsCount,
            featuredItemConstraints = featuredItemConstraints,
            thumbnailsPadding = thumbnailsPadding,
            layoutConstraints = constraints
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeFeaturedItems(
                items = featuredItems,
                rows = rows,
                columns = columns,
                featuredItemConstraints = featuredItemConstraints,
                featuredItemsPadding = featuredItemsPadding,
                thumbnailItemConstraints = thumbnailItemConstraints,
                thumbnailsPosition = thumbnailsPosition
            )

            if (thumbnailsPosition != null && thumbnailItems.isNotEmpty()) {
                placeThumbnailItems(
                    items = thumbnailItems.take(thumbnailsCount),
                    thumbnailSize = thumbnailSizePx,
                    thumbnailsPosition = thumbnailsPosition,
                    constraints = constraints
                )
            }
        }
    }
}

private fun Placeable.PlacementScope.placeFeaturedItems(
    items: List<Placeable>,
    rows: Int,
    columns: Int,
    featuredItemConstraints: Constraints,
    featuredItemsPadding: Int,
    thumbnailItemConstraints: Constraints,
    thumbnailsPosition: ThumbnailsPosition?
) {
    val startX = thumbnailsPosition?.takeIf { it == ThumbnailsPosition.Left }
        ?.let { thumbnailItemConstraints.maxWidth } ?: 0
    val startY = thumbnailsPosition?.takeIf { it == ThumbnailsPosition.Top }
        ?.let { thumbnailItemConstraints.maxHeight } ?: 0

    var x = startX
    var y = startY
    var nRow = 0

    val featuredHeight = featuredItemConstraints.maxHeight
    val featuredWidth = featuredItemConstraints.maxWidth
    items.forEachIndexed { index, placeable ->
        placeable.place(x, y)

        if (index % columns == columns - 1) {
            x = startX
            y += featuredHeight
            nRow += 1
        } else {
            x += featuredWidth
        }

        if (x == startX && nRow == rows - 1) {
            x += featuredItemsPadding
        }
    }
}

private fun measure(
    measurables: List<Measurable>,
    featuredItemConstraints: Constraints,
    thumbnailItemConstraints: Constraints
): Pair<List<Placeable>, List<Placeable>> {
    val isAnyItemPinned = measurables.all { (it.parentData as? StreamParentData)?.pin != true }

    return if (isAnyItemPinned) {
        measurables.map { it.measure(featuredItemConstraints) } to listOf()
    } else {
        val featuredItems = mutableListOf<Placeable>()
        val thumbnailItems = mutableListOf<Placeable>()
        measurables.map { measurable ->
            val isPinned = (measurable.parentData as? StreamParentData)?.pin == true
            if (isPinned) featuredItems.add(measurable.measure(featuredItemConstraints))
            else thumbnailItems.add(measurable.measure(thumbnailItemConstraints))
        }
        featuredItems to thumbnailItems
    }
}

private fun calculateFeaturedItemsPadding(
    rows: Int,
    columns: Int,
    featuredItemsCount: Int,
    featuredItemConstraints: Constraints,
    thumbnailsPadding: Int,
    layoutConstraints: Constraints
): Int {
    val lastRowFeaturedItemsCount = featuredItemsCount - (columns * (rows - 1))
    return (layoutConstraints.maxWidth - thumbnailsPadding - (lastRowFeaturedItemsCount * featuredItemConstraints.maxWidth)) / 2
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
    thumbnailSize: Int,
    thumbnailsPosition: ThumbnailsPosition,
    constraints: Constraints
) {
    val startX = when (thumbnailsPosition) {
        ThumbnailsPosition.Top, ThumbnailsPosition.Bottom -> (constraints.maxWidth - items.size * thumbnailSize) / 2
        ThumbnailsPosition.Right -> constraints.maxWidth - thumbnailSize
        ThumbnailsPosition.Left -> 0
    }
    val startY = when (thumbnailsPosition) {
        ThumbnailsPosition.Left, ThumbnailsPosition.Right -> (constraints.maxHeight - items.size * thumbnailSize) / 2
        ThumbnailsPosition.Bottom -> constraints.maxHeight - thumbnailSize
        ThumbnailsPosition.Top -> 0
    }

    if (thumbnailsPosition.isVertical()) {
        items.forEachIndexed { i, p -> p.place(x = startX + thumbnailSize * i, y = startY) }
    } else {
        items.forEachIndexed { i, p -> p.place(x = startX, y = startY + thumbnailSize * i) }
    }
}

// tablet portrait -> 1..2 -> (max 2 r, 1c), 3..4 -> (2r, 2c), 5..6 -> (3 r, 2 c), 7..8 -> (4 r, 2 c)
// tablet landscape -> 1..3 -> (1 r, max 3 c), 4 -> (2 r, 2c), 5..6 -> (2 r, 3c), 7..8 -> (2 r, 4 c)
// smartphone portrait -> 1..3 -> (max 3 r, 1c), 4 -> (2r, 2c), 5..6 -> (3 r, 2 c), 7..8 -> (4 r, 2 c)
// smartphone landscape -> 1..3 -> (1 r, max 3 c), 4 -> (2 r, 2c), 5..6 -> (2 r, 3c), 7..8 -> (2 r, 4 c)
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