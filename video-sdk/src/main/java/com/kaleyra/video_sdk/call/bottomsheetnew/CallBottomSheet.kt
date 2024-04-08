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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalBottomSheet(
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
        {
            scope.launch {
                sheetState.collapse()
            }
        }
    }
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
            modifier = modifier.padding(16.dp),
            onSheetContentSize = { size ->
                sheetHeight = size.height.toFloat()
            },
            body = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cornerShape.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        content(this)
                    }
                }
            },
            dragHandle = { sheetDragHandle?.invoke() },
            bottomSheet = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        // TODO add nested scroll handling
//                        .nestedScroll()
                        .anchoredDraggable(
                            state = sheetState.anchoredDraggableState,
                            orientation = Orientation.Vertical,
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
                                    .semantics(mergeDescendants = true) {
                                        with(sheetState) {
                                            // TODO add accessibility string
                                            dismiss("dismiss bottom sheet") {
                                                animateToDismiss()
                                                true
                                            }
                                            if (currentValue == CallSheetValue.Collapsed) {
                                                // TODO add accessibility string
                                                expand("expand bottom sheet") {
                                                    scope.launch { expand() }
                                                    true
                                                }
                                            } else {
                                                // TODO add accessibility string
                                                collapse("collapse bottom sheet") {
                                                    scope.launch { collapse() }
                                                    true
                                                }
                                            }
                                        }
                                    }
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
    dragHandle: @Composable () -> Unit,
    bottomSheet: @Composable () -> Unit,
    sheetOffset: Float,
    onSheetContentSize: (IntSize) -> Unit
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        val sheetOffsetY = sheetOffset.toInt()

        val handlePlaceable =
            subcompose(CallBottomSheetLayoutSlots.DragHandle, dragHandle)[0].measure(constraints)
        val bodyPlaceable =
            subcompose(CallBottomSheetLayoutSlots.Body, body)[0].measure(constraints)
        val sheetPlaceable =
            subcompose(CallBottomSheetLayoutSlots.Sheet, bottomSheet)[0].measure(constraints)

        val maxSheetHeight = handlePlaceable.height - sheetOffsetY
        val resizedSheetPlaceable =
            subcompose(CallBottomSheetLayoutSlots.ResizedSheet, bottomSheet)[0].measure(constraints.copy(maxHeight = maxSheetHeight))

        onSheetContentSize(IntSize(sheetPlaceable.width, sheetPlaceable.height - handlePlaceable.height))

        val layoutWidth = bodyPlaceable.width
        val layoutHeight = bodyPlaceable.height + handlePlaceable.height
        layout(layoutWidth, layoutHeight) {
            resizedSheetPlaceable.placeRelative(0, sheetOffsetY)
            bodyPlaceable.placeRelative(0, handlePlaceable.height)
        }
    }
}

enum class CallBottomSheetLayoutSlots {
    DragHandle,
    Body,
    Sheet,
    ResizedSheet,
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
