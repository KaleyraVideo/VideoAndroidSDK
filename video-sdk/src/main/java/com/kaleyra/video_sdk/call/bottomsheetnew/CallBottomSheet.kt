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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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
    val dragOrientation = Orientation.Vertical
    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember {
        { scope.launch { sheetState.settle(it) } }
    }

    Box(Modifier.fillMaxSize()) {
        Scrim(
            color = sheetScrimColor,
            onDismissRequest = animateToDismiss,
            visible = sheetState.targetValue == CallSheetValue.Expanded
        )
        Column(modifier.clip(cornerShape)) {
            Surface(
                modifier = Modifier
                    .offset {
                        val offset = if (!sheetState.offset.isNaN()) sheetState
                            .requireOffset()
                            .roundToInt() else 0
                        IntOffset(x = 0, y = offset)
                    }
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
                shape = cornerShape.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
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
                        Column(
                            modifier = Modifier.onSizeChanged {
                                val newAnchors = DraggableAnchors {
                                    CallSheetValue.Expanded at 0f
                                    CallSheetValue.Collapsed at it.height.toFloat()
                                }
                                sheetState.anchoredDraggableState.updateAnchors(newAnchors)
                            },
                            content = sheetContent
                        )
                    }
                }
            )
            Surface {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = content
                )
            }
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
    val dragOrientation = Orientation.Horizontal
    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = remember(scope) {
        { scope.launch { sheetState.collapse() } }
    }
    val settleToDismiss: (velocity: Float) -> Unit = remember(scope) {
        { scope.launch { sheetState.settle(it) } }
    }

    Box(Modifier.fillMaxSize()) {
        Scrim(
            color = sheetScrimColor,
            onDismissRequest = animateToDismiss,
            visible = sheetState.targetValue == CallSheetValue.Expanded
        )
        Row(modifier.clip(cornerShape)) {
            Surface(
                modifier = Modifier
                    .offset {
                        val offset = if (!sheetState.offset.isNaN()) sheetState
                            .requireOffset()
                            .roundToInt() else 0
                        IntOffset(x = offset, y = 0)
                    }
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
                shape = cornerShape.copy(topEnd = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
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
                        Row(
                            modifier = Modifier.onSizeChanged {
                                val newAnchors = DraggableAnchors {
                                    CallSheetValue.Expanded at 0f
                                    CallSheetValue.Collapsed at it.width.toFloat()
                                }
                                sheetState.anchoredDraggableState.updateAnchors(newAnchors)
                            },
                            content = sheetContent
                        )
                    }
                }
            )
            Surface {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    content = content
                )
            }
        }
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
