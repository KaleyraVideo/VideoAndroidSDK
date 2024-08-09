package com.kaleyra.video_sdk.call.callscreenscaffold

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Stable
internal object VCallScreenScaffoldDefaults {

    val SheetPanelContentPadding = 8.dp

    val SheetElevation = 2.dp
}

// Parameters
// modifier: Modifier for customizing the scaffold's appearance.
// topAppBar: Composable function to display the top app bar.
// sheetContent: Composable function to display content within the bottom sheet.
// sheetPanelContent: Optional composable function to display content in a panel above the bottom sheet.
// sheetDragContent: Composable function to display content in the draggable area of the bottom sheet.
// sheetState: State of the bottom sheet (collapsed, expanded, etc.).
// sheetScrimColor: Color of the scrim (overlay) when the sheet is expanded.
// sheetDragHandle: Optional composable function to display a drag handle for the bottom sheet.
// sheetCornerShape: Shape of the bottom sheet's corners.
// containerColor: Background color of the scaffold.
// contentColor: Color of the content within the scaffold.
// paddingValues: Padding values for the scaffold's content.
// content: Composable function to display the main content of the screen.

// Structure
// Surface: The scaffold is wrapped in a Surface composable, providing a background and content color.
// Box: A Box is used to position elements within the scaffold.
// Content Area: The main content is displayed within a Box with padding.
// Top App Bar: Another Box is used to display the top app bar, and its height is used to adjust padding.
// Scrim: A Scrim is displayed when the bottom sheet is expanded, allowing the user to dismiss it by tapping.
// Bottom Sheet: A Column is used to position the bottom sheet elements.
//  Sheet Panel Content: If provided, sheetPanelContent is displayed above the draggable sheet.
//  CallBottomSheetLayout: This composable handles the layout of the bottom sheet.
//  Sheet Content: The main content of the bottom sheet.
//  Sheet Drag Content: If sheetDragHandle is provided, a draggable area with the handle and sheetDragContent is displayed. This area uses anchoredDraggable to enable dragging and nestedScroll to handle scrolling within the sheet.
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun VCallScreenScaffold(
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetPanelContent: @Composable (ColumnScope.() -> Unit)? = null,
    sheetDragContent: @Composable ColumnScope.() -> Unit,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.HDragHandle() },
    sheetCornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    paddingValues: PaddingValues = CallScreenScaffoldDefaults.PaddingValues,
    content: @Composable (PaddingValues) -> Unit
) {
    val dragOrientation = Orientation.Vertical

    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember {
        { scope.launch { sheetState.settle(it) } }
    }

    val density = LocalDensity.current
    var sheetDragContentHeight by remember { mutableStateOf(0.dp) }
    var bottomSheetPadding by remember { mutableStateOf(0.dp) }
    var topAppBarPadding by remember { mutableStateOf(0.dp) }
    val contentPaddingValues by remember {
        derivedStateOf { PaddingValues(top = topAppBarPadding, bottom = bottomSheetPadding) }
    }

    val layoutDirection = LocalLayoutDirection.current
    val topPadding = paddingValues.calculateTopPadding()
    val bottomPadding = paddingValues.calculateBottomPadding()
    val startPadding = paddingValues.calculateStartPadding(layoutDirection)
    val endPadding = paddingValues.calculateEndPadding(layoutDirection)

    if (sheetPanelContent != null) {
        LaunchedEffect(Unit) {
            sheetState.collapse()
        }
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
                    .padding(start = startPadding, top = topPadding, end = endPadding)
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
            Column(Modifier.align(Alignment.BottomCenter)) {
                if (sheetPanelContent != null) {
                    Column(
                        modifier = Modifier
                            .padding(start = startPadding, end = endPadding)
                            .align(Alignment.End),
                        content = sheetPanelContent
                    )
                    Spacer(Modifier.height(VCallScreenScaffoldDefaults.SheetPanelContentPadding))
                }
                CallBottomSheetLayout(
                    modifier = Modifier
                        .onSizeChanged {
                            val height = with(density) { it.height.toDp() }
                            bottomSheetPadding = height - (sheetDragContentHeight.takeIf { sheetDragHandle != null } ?: 0.dp)
                        }
                        .padding(start = startPadding, bottom = bottomPadding, end = endPadding)
                        .clip(sheetCornerShape),
                    sheetContent = {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = VCallScreenScaffoldDefaults.SheetElevation,
                            modifier = Modifier.anchoredDraggable(
                                state = sheetState.anchoredDraggableState,
                                orientation = dragOrientation,
                                enabled = sheetDragHandle != null
                            )
                        ) {
                            Column(content = sheetContent)
                        }
                    },
                    sheetDragContent = sheetDragHandle?.let { dragHandle ->
                        {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = VCallScreenScaffoldDefaults.SheetElevation,
                                modifier = Modifier
                                    .dragVerticalOffset(sheetState)
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
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = CornerSize(0.dp)
                                ),
                                content = {
                                    Column {
                                        Box(
                                            Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .dragHandleSemantics(
                                                    sheetState = sheetState,
                                                    coroutineScope = scope,
                                                    onDismiss = animateToDismiss
                                                )
                                        ) {
                                            dragHandle()
                                        }
                                        Column(
                                            modifier = Modifier.onSizeChanged {
                                                if (it.height == 0) return@onSizeChanged
                                                val newAnchors = DraggableAnchors {
                                                    CallSheetValue.Expanded at 0f
                                                    CallSheetValue.Collapsed at it.height.toFloat()
                                                }
                                                sheetDragContentHeight = with(density) { it.height.toDp() }
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
            }
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
        val bottomSheet = sheetMeasurable?.measure(constraints.copy(minWidth = body.width, maxWidth = body.width))

        val sheetHeight = bottomSheet?.height ?: 0
        val width = body.width
        val height = body.height + sheetHeight
        layout(width, height) {
            bottomSheet?.placeRelative(0, 0)
            body.placeRelative(0, sheetHeight)
        }
    }
}

private fun Modifier.dragVerticalOffset(sheetState: CallSheetState): Modifier {
    return offset {
        val offset = if (!sheetState.offset.isNaN()) sheetState
            .requireOffset()
            .roundToInt() else 0
        IntOffset(x = 0, y = offset)
    }
}


