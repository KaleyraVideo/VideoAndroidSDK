package com.kaleyra.video_sdk.call.bottomsheetnew

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallBottomSheet(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.DragHandle() },
    cornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember(scope) {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember(scope) {
        { scope.launch { sheetState.settle(it) } }
    }

    val dragOrientation = Orientation.Vertical
    var sheetHeight by remember { mutableFloatStateOf(0f) }
    val anchors by remember {
        derivedStateOf {
            DraggableAnchors {
                CallSheetValue.Expanded at -sheetHeight
                CallSheetValue.Collapsed at 0f
            }
        }
    }

    SideEffect {
        sheetState.anchoredDraggableState.updateAnchors(anchors)
    }

    Box(Modifier.fillMaxSize()) {
        Scrim(
            color = sheetScrimColor,
            onDismissRequest = animateToDismiss,
            visible = sheetState.targetValue == CallSheetValue.Expanded
        )
        CallBottomSheetLayout(
            modifier = modifier,
            onSheetContentHeight = { sheetHeight = it.toFloat() },
            body = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = if (sheetDragHandle == null) cornerShape else cornerShape.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp)
                    )
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        content(this)
                    }
                }
            },
            dragHandle = sheetDragHandle,
            bottomSheet = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(
                            CallBottomSheetNestedScrollConnection(
                                sheetState = sheetState,
                                orientation = dragOrientation,
                                onFling = settleToDismiss
                            )
                        )
                        .anchoredDraggable(
                            state = sheetState.anchoredDraggableState,
                            orientation = dragOrientation,
                            enabled = sheetDragHandle != null
                        ),
                    shape = cornerShape.copy(
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                    content = {
                        Column(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .dragHandleSemantics(
                                        sheetState = sheetState,
                                        coroutineScope = scope,
                                        onDismiss = animateToDismiss
                                    )
                            ) {
                                sheetDragHandle?.invoke()
                            }
                            sheetContent()
                        }
                    }
                )
            },
            sheetOffset = if (!sheetState.offset.isNaN()) sheetState.requireOffset() else 0f
        )
    }
}

@Composable
private fun CallBottomSheetLayout(
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
    dragHandle: @Composable (() -> Unit)?,
    bottomSheet: @Composable () -> Unit,
    sheetOffset: Float,
    onSheetContentHeight: (Int) -> Unit
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        val sheetOffsetY = sheetOffset.toInt()
        val dragHandleHeight = dragHandle?.let { subcompose(CallBottomSheetLayoutSlots.DragHandle, dragHandle)[0].measure(constraints) }?.height ?: 0

        val bodyPlaceable = subcompose(CallBottomSheetLayoutSlots.Body, body)[0].measure(constraints)
        val sheetPlaceable = if (dragHandleHeight != 0) {
            // measure the actual sheet height
            val placeable = subcompose(CallBottomSheetLayoutSlots.Sheet, bottomSheet)[0].measure(constraints)
            // invoke the callback of the sheet content height
            onSheetContentHeight(placeable.height - dragHandleHeight)
            // remeasure the sheet composable by modifying the max height constraint
            subcompose(CallBottomSheetLayoutSlots.ResizedSheet, bottomSheet)[0].measure(
                constraints.copy(maxHeight = dragHandleHeight - sheetOffsetY)
            )
        } else null

        val layoutWidth = bodyPlaceable.width
        val layoutHeight = bodyPlaceable.height + dragHandleHeight
        layout(layoutWidth, layoutHeight) {
            sheetPlaceable?.placeRelative(0, sheetOffsetY)
            bodyPlaceable.placeRelative(0, dragHandleHeight)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalCallBottomSheet(
    sheetContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.VerticalDragHandle() },
    cornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    content: @Composable RowScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember(scope) {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember(scope) {
        { scope.launch { sheetState.settle(it) } }
    }

    val dragOrientation = Orientation.Horizontal
    var sheetWidth by remember { mutableFloatStateOf(0f) }
    val anchors by remember {
        derivedStateOf {
            DraggableAnchors {
                CallSheetValue.Expanded at -sheetWidth
                CallSheetValue.Collapsed at 0f
            }
        }
    }

    SideEffect {
        sheetState.anchoredDraggableState.updateAnchors(anchors)
    }

    Box(Modifier.fillMaxSize()) {
        Scrim(
            color = sheetScrimColor,
            onDismissRequest = animateToDismiss,
            visible = sheetState.targetValue == CallSheetValue.Expanded
        )
        VerticalCallBottomSheetLayout(
            modifier = modifier,
            onSheetContentWidth = { sheetWidth = it.toFloat() },
            body = {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    shape = if (sheetDragHandle == null) cornerShape else cornerShape.copy(
                        topStart = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    )
                ) {
                    Row(Modifier.fillMaxHeight()) {
                        content(this)
                    }
                }
            },
            dragHandle = sheetDragHandle,
            bottomSheet = {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .nestedScroll(
                            CallBottomSheetNestedScrollConnection(
                                sheetState = sheetState,
                                orientation = dragOrientation,
                                onFling = settleToDismiss
                            )
                        )
                        .anchoredDraggable(
                            state = sheetState.anchoredDraggableState,
                            orientation = dragOrientation,
                            enabled = sheetDragHandle != null
                        ),
                    shape = cornerShape.copy(
                        topEnd = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                    content = {
                        Row(Modifier.fillMaxHeight()) {
                            Box(
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .dragHandleSemantics(
                                        sheetState = sheetState,
                                        coroutineScope = scope,
                                        onDismiss = animateToDismiss
                                    )
                            ) {
                                sheetDragHandle?.invoke()
                            }
                            sheetContent()
                        }
                    }
                )
            },
            sheetOffset = if (!sheetState.offset.isNaN()) sheetState.requireOffset() else 0f
        )
    }
}

@Composable
private fun VerticalCallBottomSheetLayout(
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
    dragHandle: @Composable (() -> Unit)?,
    bottomSheet: @Composable () -> Unit,
    sheetOffset: Float,
    onSheetContentWidth: (Int) -> Unit
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        val sheetOffsetX = sheetOffset.toInt()
        val dragHandleWidth = dragHandle?.let { subcompose(CallBottomSheetLayoutSlots.DragHandle, dragHandle)[0].measure(constraints) }?.width ?: 0

        val bodyPlaceable = subcompose(CallBottomSheetLayoutSlots.Body, body)[0].measure(constraints)
        val sheetPlaceable = if (dragHandleWidth != 0) {
            // measure the actual sheet height
            val placeable = subcompose(CallBottomSheetLayoutSlots.Sheet, bottomSheet)[0].measure(constraints)
            // invoke the callback of the sheet content height
            onSheetContentWidth(placeable.width - dragHandleWidth)
            // remeasure the sheet composable by modifying the max height constraint
            subcompose(CallBottomSheetLayoutSlots.ResizedSheet, bottomSheet)[0].measure(
                constraints.copy(maxWidth = dragHandleWidth - sheetOffsetX)
            )
        } else null

        val layoutWidth = bodyPlaceable.width + dragHandleWidth
        val layoutHeight = bodyPlaceable.height
        layout(layoutWidth, layoutHeight) {
            sheetPlaceable?.placeRelative(sheetOffsetX, 0)
            bodyPlaceable.placeRelative(dragHandleWidth, 0)
        }
    }
}

enum class CallBottomSheetLayoutSlots {
    DragHandle,
    Body,
    Sheet,
    ResizedSheet
}

@Composable
private fun Scrim(
    color: Color,
    onDismissRequest: () -> Unit,
    visible: Boolean
) {
    if (color.isSpecified) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = TweenSpec(),
            label = "alpha"
        )
        val dismissSheet = if (visible) {
            Modifier
                .pointerInput(onDismissRequest) {
                    detectTapGestures {
                        onDismissRequest()
                    }
                }
                .clearAndSetSemantics {}
        } else {
            Modifier
        }
        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissSheet)
        ) {
            drawRect(color = color, alpha = alpha)
        }
    }
}

private fun Modifier.dragHandleSemantics(
    sheetState: CallSheetState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit
): Modifier =
    semantics(mergeDescendants = true) {
        with(sheetState) {
            // TODO add accessibility string
            dismiss("dismiss bottom sheet") {
                onDismiss()
                true
            }
            if (currentValue == CallSheetValue.Collapsed) {
                // TODO add accessibility string
                expand("expand bottom sheet") {
                    coroutineScope.launch { expand() }
                    true
                }
            } else {
                // TODO add accessibility string
                collapse("collapse bottom sheet") {
                    coroutineScope.launch { collapse() }
                    true
                }
            }
        }
    }

