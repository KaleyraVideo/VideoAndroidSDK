package com.kaleyra.video_sdk.call.screen.view

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import androidx.compose.ui.unit.round
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeWidth
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Immutable
enum class ThumbnailsArrangement {
    Top, Left, Bottom, Right;

    fun isHorizontal() = this == Left || this == Right

    fun isVertical() = this == Top || this == Bottom
}

@Composable
fun isPortraitOrientation(): State<Boolean> {
    val configuration = LocalConfiguration.current
    return remember {
        derivedStateOf {
            configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        }
    }
}

@Composable
fun StreamGrid(
    modifier: Modifier = Modifier,
    maxWidth: Dp,
    thumbnailSize: Dp,
    thumbnailsArrangement: ThumbnailsArrangement,
    thumbnailsCount: Int,
    streams: @Composable () -> Unit
) {
    val thumbnailSizePx = with(LocalDensity.current) { thumbnailSize.roundToPx() }
    val isPortrait by isPortraitOrientation()

    Layout(
        content = streams,
        modifier = modifier
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            "unbounded size not supported"
        }

        val pinnedCount = measurables.count { (it.parentData as? StreamParentData)?.pin == true }
        val featuredCount = pinnedCount.takeIf { it != 0 } ?: measurables.size
        val layoutThumbnailsArrangement = thumbnailsArrangement.takeIf { pinnedCount > 0 }

        val (rows, columns) = calculateRowsAndColumns(isPortrait, maxWidth, featuredCount)

        val featuredWidth = calculateFeaturedItemsWidth(layoutThumbnailsArrangement, thumbnailSizePx, columns, constraints)
        val featuredHeight = calculateFeaturedItemsHeight(layoutThumbnailsArrangement, thumbnailSizePx, rows, constraints)

        val featuredConstraints = constraints.copy(maxWidth = featuredWidth, maxHeight = featuredHeight)
        val thumbnailConstraints = Constraints.fixed(thumbnailSizePx, thumbnailSizePx)

        val (featuredPlaceables, thumbnailPlaceables) = measure(measurables, featuredConstraints, thumbnailConstraints)

        val thumbnailsPadding = if (layoutThumbnailsArrangement?.isHorizontal() == true) thumbnailSizePx else 0
        val featuredItemsPadding = calculateFeaturedItemsPadding(
            rows = rows,
            columns = columns,
            featuredCount = featuredCount,
            featuredConstraints = featuredConstraints,
            thumbnailsPadding = thumbnailsPadding,
            layoutConstraints = constraints
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeFeatured(
                placeables = featuredPlaceables,
                rows = rows,
                columns = columns,
                featuredConstraints = featuredConstraints,
                featuredPadding = featuredItemsPadding,
                thumbnailConstraints = thumbnailConstraints,
                thumbnailsArrangement = layoutThumbnailsArrangement
            )

            if (layoutThumbnailsArrangement != null && thumbnailPlaceables.isNotEmpty()) {
                placeThumbnails(
                    placeables = thumbnailPlaceables.take(thumbnailsCount),
                    thumbnailSize = thumbnailSizePx,
                    thumbnailsArrangement = layoutThumbnailsArrangement,
                    constraints = constraints
                )
            }
        }
    }
}

private fun Placeable.PlacementScope.placeFeatured(
    placeables: List<Placeable>,
    rows: Int,
    columns: Int,
    featuredConstraints: Constraints,
    featuredPadding: Int,
    thumbnailConstraints: Constraints,
    thumbnailsArrangement: ThumbnailsArrangement?
) {
    val startX = thumbnailsArrangement?.takeIf { it == ThumbnailsArrangement.Left }
        ?.let { thumbnailConstraints.maxWidth } ?: 0
    val startY = thumbnailsArrangement?.takeIf { it == ThumbnailsArrangement.Top }
        ?.let { thumbnailConstraints.maxHeight } ?: 0

    var x = startX
    var y = startY
    var nRow = 0

    val featuredHeight = featuredConstraints.maxHeight
    val featuredWidth = featuredConstraints.maxWidth
    placeables.forEachIndexed { index, placeable ->
        placeable.place(x, y)

        if (index % columns == columns - 1) {
            x = startX
            y += featuredHeight
            nRow += 1
        } else {
            x += featuredWidth
        }

        if (x == startX && nRow == rows - 1) {
            x += featuredPadding
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
    featuredCount: Int,
    featuredConstraints: Constraints,
    thumbnailsPadding: Int,
    layoutConstraints: Constraints
): Int {
    val lastRowFeaturedItemsCount = featuredCount - (columns * (rows - 1))
    return (layoutConstraints.maxWidth - thumbnailsPadding - (lastRowFeaturedItemsCount * featuredConstraints.maxWidth)) / 2
}

private fun calculateFeaturedItemsWidth(
    thumbnailsArrangement: ThumbnailsArrangement?,
    thumbnailSize: Int,
    columns: Int,
    constraints: Constraints
): Int {
    return if (thumbnailsArrangement == null || thumbnailsArrangement.isVertical()) constraints.maxWidth / columns
    else (constraints.maxWidth - thumbnailSize) / columns
}

private fun calculateFeaturedItemsHeight(
    thumbnailsArrangement: ThumbnailsArrangement?,
    thumbnailSize: Int,
    rows: Int,
    constraints: Constraints
): Int {
    return if (thumbnailsArrangement == null || thumbnailsArrangement.isHorizontal()) constraints.maxHeight / rows
    else (constraints.maxHeight - thumbnailSize) / rows
}

private fun Placeable.PlacementScope.placeThumbnails(
    placeables: List<Placeable>,
    thumbnailSize: Int,
    thumbnailsArrangement: ThumbnailsArrangement,
    constraints: Constraints
) {
    val startX = when (thumbnailsArrangement) {
        ThumbnailsArrangement.Top, ThumbnailsArrangement.Bottom -> (constraints.maxWidth - placeables.size * thumbnailSize) / 2
        ThumbnailsArrangement.Right -> constraints.maxWidth - thumbnailSize
        ThumbnailsArrangement.Left -> 0
    }
    val startY = when (thumbnailsArrangement) {
        ThumbnailsArrangement.Left, ThumbnailsArrangement.Right -> (constraints.maxHeight - placeables.size * thumbnailSize) / 2
        ThumbnailsArrangement.Bottom -> constraints.maxHeight - thumbnailSize
        ThumbnailsArrangement.Top -> 0
    }

    if (thumbnailsArrangement.isVertical()) {
        placeables.forEachIndexed { i, p -> p.place(x = startX + thumbnailSize * i, y = startY) }
    } else {
        placeables.forEachIndexed { i, p -> p.place(x = startX, y = startY + thumbnailSize * i) }
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
fun Modifier.animateConstraints(
    animationSpec: AnimationSpec<IntSize> = spring()
) = composed {
    var animatable: Animatable<IntSize, AnimationVector2D>? by remember { mutableStateOf(null) }
    var targetSize: IntSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        snapshotFlow { targetSize }.collect { target ->
            val anim = animatable ?: Animatable(target, IntSize.VectorConverter).also {
                animatable = it
            }
            if (anim.targetValue != target) {
                launch { anim.animateTo(target, animationSpec) }
            }
        }
    }

    this@composed.intermediateLayout { measurable, _ ->
        targetSize = lookaheadSize
        val (width, height) = animatable?.value ?: lookaheadSize
        val constraints = Constraints.fixed(width, height)

        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

fun Modifier.animatePlacement(
    initialOffset: IntOffset = IntOffset.Zero,
    animationSpec: AnimationSpec<IntOffset> = spring(stiffness = StiffnessMediumLow)
): Modifier = composed {
    val scope = rememberCoroutineScope()
    var animatable: Animatable<IntOffset, AnimationVector2D>? by remember { mutableStateOf(null) }
    var targetOffset by remember { mutableStateOf(initialOffset) }

    this
        .onPlaced {
            targetOffset = it
                .positionInParent()
                .round()
        }
        .offset {
            val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter).also {
                animatable = it
            }
            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(targetOffset, animationSpec)
                }
            }
            anim.value - targetOffset
        }
}

@LayoutScopeMarker
@Immutable
object StreamGridScope {

    @Stable
    fun Modifier.pin(value: Boolean): Modifier {
        return then(StreamParentData(pin = value))
    }
}

class StreamParentData(
    val pin: Boolean
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@StreamParentData
}