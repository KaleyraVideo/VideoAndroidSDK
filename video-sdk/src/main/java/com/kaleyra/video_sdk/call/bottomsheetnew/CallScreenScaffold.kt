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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object CallScreenScaffoldDefaults {

    val paddingValues = PaddingValues(16.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CallScreenScaffold(
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetPanelContent: @Composable (ColumnScope.() -> Unit)? = null,
    sheetDragContent: @Composable ColumnScope.() -> Unit,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.DragHandle() },
    sheetCornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    paddingValues: PaddingValues = CallScreenScaffoldDefaults.paddingValues,
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
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.padding(start = startPadding, top = topPadding, end = endPadding)) {
                content(contentPaddingValues)
                Box(
                    modifier = Modifier
                        .onSizeChanged {
                            topAppBarPadding = with(density) { it.height.toDp() }
                        },
                    content = { topAppBar() }
                )
            }
            Scrim(
                color = sheetScrimColor,
                onDismissRequest = animateToDismiss,
                visible = sheetState.targetValue == CallSheetValue.Expanded
            )
            Column(Modifier.align(Alignment.BottomCenter)) {
                if (sheetPanelContent != null) {
                    Card(
                        modifier = Modifier
                            .padding(start = startPadding, end = endPadding)
                            .align(Alignment.End)
                            .dragVerticalOffset(sheetState),
                        content = sheetPanelContent
                    )
                    Spacer(Modifier.height(8.dp))
                }
                CallBottomSheetLayout(
                    modifier = Modifier
                        .onSizeChanged {
                            val height = with(density) { it.height.toDp() }
                            bottomSheetPadding = height - sheetDragContentHeight
                        }
                        .padding(start = startPadding, bottom = bottomPadding, end = endPadding)
                        .clip(sheetCornerShape)
                        .anchoredDraggable(
                            state = sheetState.anchoredDraggableState,
                            orientation = dragOrientation
                        ),
                    sheetContent = {
                        Surface {
                            Column(content = sheetContent)
                        }
                    },
                    sheetDragContent = sheetDragHandle?.let { dragHandle ->
                        {
                            Surface(
                                modifier = Modifier
                                    .dragVerticalOffset(sheetState)
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
                                                val newAnchors = DraggableAnchors {
                                                    CallSheetValue.Expanded at 0f
                                                    CallSheetValue.Collapsed at it.height.toFloat()
                                                }
                                                sheetDragContentHeight =
                                                    with(density) { it.height.toDp() }
                                                sheetState.anchoredDraggableState.updateAnchors(
                                                    newAnchors
                                                )
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
internal fun CallBottomSheetLayout(
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
        val bottomSheet =
            sheetMeasurable?.measure(constraints.copy(minWidth = body.width, maxWidth = body.width))

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CallScreenLandscapeScaffold(
    modifier: Modifier = Modifier,
    topAppBar: @Composable () -> Unit,
    sheetContent: @Composable RowScope.() -> Unit,
    sheetDragContent: @Composable RowScope.() -> Unit,
    sheetState: CallSheetState = rememberCallSheetState(),
    sheetScrimColor: Color = CallBottomSheetDefaults.ScrimColor,
    sheetDragHandle: @Composable (() -> Unit)? = { CallBottomSheetDefaults.VerticalDragHandle() },
    sheetCornerShape: RoundedCornerShape = CallBottomSheetDefaults.Shape,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    paddingValues: PaddingValues = CallScreenScaffoldDefaults.paddingValues,
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

    val density = LocalDensity.current
    var sheetDragContentWidth by remember { mutableStateOf(0.dp) }
    var bottomSheetPadding by remember { mutableStateOf(0.dp) }
    var topAppBarPadding by remember { mutableStateOf(0.dp) }
    val contentPaddingValues by remember {
        derivedStateOf { PaddingValues(top = topAppBarPadding, end = bottomSheetPadding) }
    }

    val layoutDirection = LocalLayoutDirection.current
    val topPadding = paddingValues.calculateTopPadding()
    val bottomPadding = paddingValues.calculateBottomPadding()
    val startPadding = paddingValues.calculateStartPadding(layoutDirection)
    val endPadding = paddingValues.calculateEndPadding(layoutDirection)
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.padding(start = startPadding, top = topPadding, bottom = bottomPadding)) {
                content(contentPaddingValues)
                Box(
                    modifier = Modifier
                        .padding(end = bottomSheetPadding)
                        .onSizeChanged {
                            topAppBarPadding = with(density) { it.height.toDp() }
                        },
                    content = { topAppBar() }
                )
            }
            Scrim(
                color = sheetScrimColor,
                onDismissRequest = animateToDismiss,
                visible = sheetState.targetValue == CallSheetValue.Expanded
            )
            CallBottomSheetVerticalLayout(
                modifier = Modifier
                    .onSizeChanged {
                        val width = with(density) { it.width.toDp() }
                        bottomSheetPadding = width - sheetDragContentWidth
                    }
                    .align(Alignment.CenterEnd)
                    .padding(top = topPadding, bottom = bottomPadding, end = endPadding)
                    .clip(sheetCornerShape)
                    .anchoredDraggable(
                        state = sheetState.anchoredDraggableState,
                        orientation = dragOrientation
                    ),
                sheetContent = {
                    Surface {
                        Row(content = sheetContent)
                    }
                },
                sheetDragContent = sheetDragHandle?.let { dragHandle ->
                    {
                        Surface(
                            modifier = Modifier
                                .dragHorizontalOffset(sheetState)
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
                                            val newAnchors = DraggableAnchors {
                                                CallSheetValue.Expanded at 0f
                                                CallSheetValue.Collapsed at it.width.toFloat()
                                            }
                                            sheetDragContentWidth =
                                                with(density) { it.width.toDp() }
                                            sheetState.anchoredDraggableState.updateAnchors(
                                                newAnchors
                                            )
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

@Composable
internal fun CallBottomSheetVerticalLayout(
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
        val bottomSheet = sheetMeasurable?.measure(
            constraints.copy(
                minHeight = body.height,
                maxHeight = body.height
            )
        )

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
            dismiss {
                onDismiss()
                true
            }
            if (currentValue == CallSheetValue.Collapsed) {
                expand {
                    coroutineScope.launch { expand() }
                    true
                }
            } else {
                collapse {
                    coroutineScope.launch { collapse() }
                    true
                }
            }
        }
    }
