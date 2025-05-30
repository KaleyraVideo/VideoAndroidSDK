package com.kaleyra.video_sdk.call.callscreenscaffold

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheet.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheet.CallBottomSheetNestedScrollConnection
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.rememberCallSheetState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HCallScreenScaffold(
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit,
    sheetContent: @Composable RowScope.() -> Unit,
    sheetDragContent: @Composable RowScope.() -> Unit,
    brandLogo: @Composable BoxScope.() -> Unit,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.VDragHandle() },
    sheetCornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = contentColorFor(containerColor),
    paddingValues: PaddingValues = CallScreenScaffoldDefaults.PaddingValues,
    content: @Composable (PaddingValues) -> Unit
) {
    val dragOrientation = Orientation.Horizontal

    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember {
        { scope.launch { sheetState.settle(it) } }
    }

    val layoutDirection = LocalLayoutDirection.current
    val topPadding = paddingValues.calculateTopPadding()
    val bottomPadding = paddingValues.calculateBottomPadding()
    val startPadding = paddingValues.calculateStartPadding(layoutDirection)
    val endPadding = paddingValues.calculateEndPadding(layoutDirection)

    val density = LocalDensity.current
    var sheetDragContentWidth by remember { mutableStateOf(0.dp) }
    var bottomSheetPadding by remember { mutableStateOf(0.dp) }
    var topAppBarPadding by remember { mutableStateOf(0.dp) }
    val contentPaddingValues by remember(startPadding, bottomPadding) {
        derivedStateOf { PaddingValues(start = startPadding, top = topAppBarPadding, end = bottomSheetPadding, bottom = bottomPadding) }
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(Modifier.fillMaxSize()) {
            content(contentPaddingValues)
            Box(
                modifier = Modifier
                    .padding(start = startPadding, top = topPadding, bottom = bottomPadding, end = bottomSheetPadding)
                    .onGloballyPositioned {
                        topAppBarPadding = with(density) { it.boundsInRoot().bottom.toDp() }
                    },
                content = { topAppBar() }
            )
            Scrim(
                color = sheetScrimColor,
                onDismissRequest = animateToDismiss,
                visible = sheetState.targetValue == CallSheetValue.Expanded
            )
            CallBottomSheetLayout(
                modifier = Modifier
                    .onSizeChanged {
                        val width = with(density) { it.width.toDp() }
                        bottomSheetPadding = width - (sheetDragContentWidth.takeIf { sheetDragHandle != null } ?: 0.dp)
                    }
                    .align(Alignment.CenterEnd)
                    .padding(top = topPadding, bottom = bottomPadding, end = endPadding)
                    .clip(sheetCornerShape),
                sheetContent = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.anchoredDraggable(
                            state = sheetState.anchoredDraggableState,
                            orientation = dragOrientation,
                        )
                    ) {
                        Row(content = sheetContent)
                    }
                },
                sheetDragContent = sheetDragHandle?.let { dragHandle ->
                    {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .dragHorizontalOffset(sheetState)
                                .anchoredDraggable(
                                    state = sheetState.anchoredDraggableState,
                                    orientation = dragOrientation,
                                )
                                .nestedScroll(
                                    CallBottomSheetNestedScrollConnection(
                                        sheetState = sheetState,
                                        orientation = dragOrientation,
                                        onFling = settleToDismiss
                                    )
                                ),
                            shape = sheetCornerShape.copy(
                                topEnd = CornerSize(0.dp),
                                bottomEnd = CornerSize(0.dp)
                            ),
                            content = {
                                Row {
                                    Box(
                                        Modifier
                                            .align(Alignment.CenterVertically)
                                            .dragHandleSemantics(
                                                sheetState = sheetState,
                                                coroutineScope = scope,
                                                onDismiss = animateToDismiss
                                            )
                                    ) {
                                        dragHandle.invoke()
                                    }
                                    Row(
                                        modifier = Modifier.onSizeChanged {
                                            if (it.width == 0) return@onSizeChanged
                                            val newAnchors = DraggableAnchors {
                                                CallSheetValue.Expanded at 0f
                                                CallSheetValue.Collapsed at it.width.toFloat()
                                            }
                                            sheetDragContentWidth = with(density) { it.width.toDp() }
                                            sheetState.anchoredDraggableState.updateAnchors(newAnchors)
                                        },
                                        content = sheetDragContent
                                    )
                                }
                            }
                        )
                    }
                }
            )
            brandLogo()
        }
    }
}

@Composable
private fun CallBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetDragContent: @Composable (() -> Unit)?,
    sheetContent: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = {
            sheetContent()
            sheetDragContent?.invoke()
        }
    ) { measurables, constraints ->
        val bodyMeasurable = measurables[0]
        val sheetMeasurable = measurables.getOrNull(1)
        val body = bodyMeasurable.measure(constraints)
        val bottomSheet = sheetMeasurable?.measure(constraints.copy(minHeight = body.height, maxHeight = body.height))

        val sheetWidth = bottomSheet?.width ?: 0
        val width = body.width + sheetWidth
        val height = body.height
        layout(width, height) {
            bottomSheet?.placeRelative(0, 0)
            body.placeRelative(sheetWidth, 0)
        }
    }
}

private fun Modifier.dragHorizontalOffset(sheetState: CallSheetState): Modifier {
    return offset {
        val offset = if (!sheetState.offset.isNaN()) sheetState
            .requireOffset()
            .roundToInt() else 0
        IntOffset(x = offset, y = 0)
    }
}