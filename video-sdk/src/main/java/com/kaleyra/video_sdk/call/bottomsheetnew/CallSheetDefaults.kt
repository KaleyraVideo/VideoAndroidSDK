package com.kaleyra.video_sdk.call.bottomsheetnew

import android.content.res.Resources
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object AnchoredDraggableCallSheetDefaults {

    val AnimationSpec = TweenSpec<Float>(easing = LinearOutSlowInEasing)

    val VelocityThreshold: () -> Float = { 125.dp.toPixel }

    val PositionalThreshold: (distance: Float) -> Float = { it * 0.5f }
}

object CallBottomSheetDefaults {

    val Shape: RoundedCornerShape = RoundedCornerShape(22.dp)

    val ScrimColor: Color @Composable get() = MaterialTheme.colorScheme.scrim.copy(ScrimOpacity)

    val ScrimOpacity = .32f

    @Composable
    fun HDragHandle(
        modifier: Modifier = Modifier,
        width: Dp = CallSheetTokens.DragHandleWidth,
        height: Dp = CallSheetTokens.DragHandleHeight,
        shape: Shape = MaterialTheme.shapes.extraLarge,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(CallSheetTokens.DragHandleOpacity)
    ) {
        // TODO load a string resource
        val dragHandleDescription = "description"
        Surface(
            modifier = modifier
                .padding(vertical = DragHandlePadding)
                .semantics { contentDescription = dragHandleDescription },
            color = color,
            shape = shape
        ) {
            Box(
                Modifier
                    .size(
                        width = width,
                        height = height
                    )
            )
        }
    }

    @Composable
    fun VDragHandle(
        modifier: Modifier = Modifier,
        width: Dp = CallSheetTokens.VerticalDragHandleWidth,
        height: Dp = CallSheetTokens.VerticalDragHandleHeight,
        shape: Shape = MaterialTheme.shapes.extraLarge,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(CallSheetTokens.DragHandleOpacity)
    ) {
        // TODO load a string resource
        val dragHandleDescription = "description"
        Surface(
            modifier = modifier
                .padding(horizontal = DragHandlePadding)
                .semantics { contentDescription = dragHandleDescription },
            color = color,
            shape = shape
        ) {
            Box(
                Modifier
                    .size(
                        width = width,
                        height = height
                    )
            )
        }
    }
}

private val DragHandlePadding = 8.dp

internal val Dp.toPixel: Float
    get() = value * Resources.getSystem().displayMetrics.density

@Composable
internal fun rememberCallSheetState(
    confirmValueChange: (CallSheetValue) -> Boolean = { true },
    initialValue: CallSheetValue = CallSheetValue.Collapsed,
): CallSheetState {
    return rememberSaveable(
        confirmValueChange,
        saver = CallSheetState.Saver(confirmValueChange)
    ) {
        CallSheetState(initialValue, confirmValueChange)
    }
}

@Stable
@OptIn(ExperimentalFoundationApi::class)
class CallSheetState(
    initialValue: CallSheetValue = CallSheetValue.Collapsed,
    confirmValueChange: (CallSheetValue) -> Boolean = { true }
) {

    val currentValue: CallSheetValue get() = anchoredDraggableState.currentValue

    val targetValue: CallSheetValue get() = anchoredDraggableState.targetValue

    val offset: Float get() = anchoredDraggableState.offset

    fun requireOffset(): Float = anchoredDraggableState.requireOffset()

    suspend fun expand() {
        animateTo(CallSheetValue.Expanded)
    }

    suspend fun collapse() {
        animateTo(CallSheetValue.Collapsed)
    }

    internal suspend fun animateTo(
        targetValue: CallSheetValue,
        velocity: Float = anchoredDraggableState.lastVelocity
    ) {
        anchoredDraggableState.animateTo(targetValue, velocity)
    }

    internal suspend fun snapTo(targetValue: CallSheetValue) {
        anchoredDraggableState.snapTo(targetValue)
    }

    internal suspend fun settle(velocity: Float) {
        anchoredDraggableState.settle(velocity)
    }

    internal var anchoredDraggableState = AnchoredDraggableState(
        initialValue = initialValue,
        positionalThreshold = AnchoredDraggableCallSheetDefaults.PositionalThreshold,
        velocityThreshold = AnchoredDraggableCallSheetDefaults.VelocityThreshold,
        animationSpec = AnchoredDraggableCallSheetDefaults.AnimationSpec,
        confirmValueChange = confirmValueChange
    )

    companion object {
        fun Saver(confirmValueChange: (CallSheetValue) -> Boolean) =
            androidx.compose.runtime.saveable.Saver<CallSheetState, CallSheetValue>(
                save = { it.currentValue },
                restore = { savedValue ->
                    CallSheetState(savedValue, confirmValueChange)
                }
            )
    }
}

enum class CallSheetValue { Expanded, Collapsed }