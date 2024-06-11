package com.kaleyra.video_sdk.call.screen.view

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.streamnew.utils.AdaptiveGridCalculator

@Stable
internal object AdaptiveStreamLayoutDefaults {

    val ThumbnailArrangement = ThumbnailsArrangement.Bottom

    val ThumbnailSize = 96.dp

    const val ThumbnailCount = 3
}

@Immutable
internal enum class ThumbnailsArrangement {
    Top, Start, Bottom, End;

    fun isHorizontal() = this == Start || this == End

    fun isVertical() = this == Top || this == Bottom
}

@LayoutScopeMarker
@Immutable
internal object AdaptiveStreamLayoutScope {

    @Stable
    fun Modifier.pin(value: Boolean): Modifier {
        return then(StreamParentData(pin = value))
    }
}

internal class StreamParentData(
    val pin: Boolean
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@StreamParentData
}

@Composable
internal fun AdaptiveStreamLayout(
    modifier: Modifier = Modifier,
    thumbnailsArrangement: ThumbnailsArrangement = AdaptiveStreamLayoutDefaults.ThumbnailArrangement,
    thumbnailSize: Dp = AdaptiveStreamLayoutDefaults.ThumbnailSize,
    thumbnailsCount: Int = AdaptiveStreamLayoutDefaults.ThumbnailCount,
    content: @Composable AdaptiveStreamLayoutScope.() -> Unit
) {
    val thumbnailSizePx = with(LocalDensity.current) { thumbnailSize.roundToPx() }

    Layout(
        content = { AdaptiveStreamLayoutScope.content() } ,
        modifier = modifier
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            "unbounded size not supported"
        }

        val pinnedCount = measurables.count { (it.parentData as? StreamParentData)?.pin == true }
        val featuredCount = pinnedCount.takeIf { it != 0 } ?: measurables.size
        val layoutThumbnailsArrangement = thumbnailsArrangement.takeIf { pinnedCount > 0 }

        val featuredContainerWidth = if (pinnedCount != measurables.size) {
            constraints.maxWidth - if (layoutThumbnailsArrangement?.isHorizontal() == true) thumbnailSizePx else 0
        }
        // else if all streams are pinned
        else constraints.maxWidth

        val featuredContainerHeight = if (pinnedCount != measurables.size) {
            constraints.maxHeight - if (layoutThumbnailsArrangement?.isVertical() == true) thumbnailSizePx else 0
        }
        // else if all streams are pinned
        else constraints.maxHeight

        val (rows, columns, featuredSize) = calculateGridAndFeaturedSize(constraints, featuredContainerWidth, featuredContainerHeight, featuredCount)

        val featuredConstraints = constraints.copy(maxWidth = featuredSize.width, maxHeight = featuredSize.height)
        val thumbnailConstraints = Constraints.fixed(thumbnailSizePx, thumbnailSizePx)

        val (featuredPlaceables, thumbnailPlaceables) = measure(measurables, featuredConstraints, thumbnailConstraints)

        val areThumbnailsEmpty = thumbnailPlaceables.isEmpty()
        val thumbnailsPadding = if (!areThumbnailsEmpty && layoutThumbnailsArrangement?.isHorizontal() == true) thumbnailSizePx else 0

        val lastRowFeaturedItemsCount = featuredCount - (columns * (rows - 1))
        val featuredItemsPadding = (constraints.maxWidth - thumbnailsPadding - (lastRowFeaturedItemsCount * featuredConstraints.maxWidth)) / 2

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeFeatured(
                placeables = featuredPlaceables,
                rows = rows,
                columns = columns,
                featuredConstraints = featuredConstraints,
                featuredPadding = featuredItemsPadding,
                thumbnailConstraints = thumbnailConstraints,
                thumbnailsArrangement = layoutThumbnailsArrangement,
                areThumbnailsEmpty = areThumbnailsEmpty
            )

            if (layoutThumbnailsArrangement != null && !areThumbnailsEmpty) {
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

private fun calculateGridAndFeaturedSize(
    layoutConstraints: Constraints,
    containerWidth: Int,
    containerHeight: Int,
    itemsCount: Int
): Triple<Int, Int, IntSize> {
    // Apply the AdaptiveStreamLayoutHelper.calculateGridAndFeaturedSize logic only for more than 3 items
    // in order to have the same stream layout behaviour up to 3 items on both Android and iOS sdks
    return when {
        itemsCount == 0 -> Triple(1, 1, IntSize(0, 0))
        itemsCount > 3 -> AdaptiveGridCalculator.calculateGridAndFeaturedSize(containerWidth, containerHeight, itemsCount)
        layoutConstraints.maxWidth >= layoutConstraints.maxHeight -> Triple(1, itemsCount, IntSize(containerWidth / itemsCount, containerHeight))
        else -> Triple(itemsCount, 1, IntSize(containerWidth,containerHeight / itemsCount))
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

private fun Placeable.PlacementScope.placeFeatured(
    placeables: List<Placeable>,
    rows: Int,
    columns: Int,
    featuredConstraints: Constraints,
    featuredPadding: Int,
    thumbnailConstraints: Constraints,
    thumbnailsArrangement: ThumbnailsArrangement?,
    areThumbnailsEmpty: Boolean
) {
    val startX = if (!areThumbnailsEmpty && thumbnailsArrangement == ThumbnailsArrangement.Start) thumbnailConstraints.maxWidth else 0
    val startY = if (!areThumbnailsEmpty && thumbnailsArrangement == ThumbnailsArrangement.Top) thumbnailConstraints.maxHeight else 0

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

private fun Placeable.PlacementScope.placeThumbnails(
    placeables: List<Placeable>,
    thumbnailSize: Int,
    thumbnailsArrangement: ThumbnailsArrangement,
    constraints: Constraints
) {
    val startX = when (thumbnailsArrangement) {
        ThumbnailsArrangement.Top, ThumbnailsArrangement.Bottom -> (constraints.maxWidth - placeables.size * thumbnailSize) / 2
        ThumbnailsArrangement.End -> constraints.maxWidth - thumbnailSize
        ThumbnailsArrangement.Start -> 0
    }
    val startY = when (thumbnailsArrangement) {
        ThumbnailsArrangement.Start, ThumbnailsArrangement.End -> (constraints.maxHeight - placeables.size * thumbnailSize) / 2
        ThumbnailsArrangement.Bottom -> constraints.maxHeight - thumbnailSize
        ThumbnailsArrangement.Top -> 0
    }

    if (thumbnailsArrangement.isVertical()) {
        placeables.forEachIndexed { i, p -> p.place(x = startX + thumbnailSize * i, y = startY) }
    } else {
        placeables.forEachIndexed { i, p -> p.place(x = startX, y = startY + thumbnailSize * i) }
    }
}