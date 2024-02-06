package com.kaleyra.video_sdk.call.bottomsheetm3.view

import android.content.res.Resources
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberCallUiBottomSheetState(
    initialValue: CallUiBottomSheetValue,
    animationSpec: AnimationSpec<Float> = CallUiBottomSheetDefaults.AnimationSpec,
    confirmValueChange: (CallUiBottomSheetValue) -> Boolean = { true },
    onDismiss: () -> Unit = {},
): CallUiBottomSheetState {
    return key(initialValue) {
        rememberSaveable(
            initialValue, animationSpec, confirmValueChange,
            saver = CallUiBottomSheetState.Saver(
                animationSpec = animationSpec,
                confirmValueChange = confirmValueChange,
                onDismiss = onDismiss,
            )
        ) {
            CallUiBottomSheetState(
                initialValue = initialValue,
                animationSpec = animationSpec,
                confirmValueChange = confirmValueChange,
                onDismiss = onDismiss,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Stable
class CallUiBottomSheetState(
    initialValue: CallUiBottomSheetValue,
    val onDismiss: () -> Unit = {},
    private val animationSpec: AnimationSpec<Float> = CallUiBottomSheetDefaults.AnimationSpec,
    private val confirmValueChange: (CallUiBottomSheetValue) -> Boolean = { true },
) {

    val draggableState = AnchoredDraggableState(
        initialValue = initialValue,
        animationSpec = animationSpec,
        positionalThreshold = CallUiBottomSheetDefaults.PositionalThreshold,
        velocityThreshold = CallUiBottomSheetDefaults.VelocityThreshold,
        confirmValueChange = confirmValueChange
    )

    var peekOffset: Dp = 0.dp

    /**
     * The current value of the [CallUiBottomSheetValue].
     */
    val currentValue: CallUiBottomSheetValue
        get() = draggableState.currentValue

    val targetValue: CallUiBottomSheetValue
        get() = draggableState.targetValue

    /**
     * Whether the bottom sheet is visible.
     */
    val isVisible: Boolean = true

    /**
     * Whether the bottom sheet is expanded.
     */
    val isExpanded: Boolean
        get() = currentValue == CallUiBottomSheetValue.Expanded

    /**
     * Whether the bottom sheet is half expanded.
     */
    val isHalfExpanded: Boolean
        get() = currentValue == CallUiBottomSheetValue.HalfExpanded

    private val hasHalfExpandedState: Boolean
        get() = draggableState.anchors.hasAnchorFor(CallUiBottomSheetValue.HalfExpanded)

    /**
     * Show the bottom sheet with animation and suspend until it's shown.
     * If the sheet is taller than 50% of the parent's height, the bottom sheet will be half expanded.
     * Otherwise, it will be fully expanded.
     */
    suspend fun show() {
        val targetValue = when {
            hasHalfExpandedState -> CallUiBottomSheetValue.HalfExpanded
            else -> CallUiBottomSheetValue.Expanded
        }
        animateTo(targetValue)
    }

    /**
     * Expand the bottom sheet with an animation and suspend until the animation finishes or is cancelled.
     */
    suspend fun expand() {
        if (draggableState.anchors.hasAnchorFor(CallUiBottomSheetValue.Expanded)) {
            animateTo(CallUiBottomSheetValue.Expanded)
        }
    }

    /**
     * Half expand the bottom sheet with an animation and suspend until the animation finishes or is cancelled.
     */
    suspend fun halfExpand() {
        if (draggableState.anchors.hasAnchorFor(CallUiBottomSheetValue.HalfExpanded)) {
            animateTo(CallUiBottomSheetValue.HalfExpanded)
        }
    }

    fun requireOffset() = draggableState.requireOffset()

    fun updateAnchors(sheetHeight: Int) {
        val newAnchors = DraggableAnchors {
            CallUiBottomSheetValue.entries
                .forEach { anchor ->
                    when (anchor) {
                        CallUiBottomSheetValue.HalfExpanded -> {
                            anchor at sheetHeight - peekOffset.toPixel
                        }
                        CallUiBottomSheetValue.Expanded -> {
                            anchor at 0f
                        }
                    }
                }
        }
        draggableState.updateAnchors(newAnchors)
    }

    private suspend fun animateTo(
        targetValue: CallUiBottomSheetValue,
        velocity: Float = draggableState.lastVelocity
    ) = draggableState.animateTo(targetValue, velocity)

    companion object {
        /**
         * The default [Saver] implementation for [CallUiBottomSheetState].
         */
        fun Saver(
            animationSpec: AnimationSpec<Float> = CallUiBottomSheetDefaults.AnimationSpec,
            confirmValueChange: (CallUiBottomSheetValue) -> Boolean = { true },
            onDismiss: () -> Unit = {},
        ): Saver<CallUiBottomSheetState, CallUiBottomSheetValue> =
            Saver(
                save = { it.currentValue },
                restore = {
                    CallUiBottomSheetState(
                        initialValue = it,
                        animationSpec = animationSpec,
                        confirmValueChange = confirmValueChange,
                        onDismiss = onDismiss,
                    )
                }
            )
    }
}

object CallUiBottomSheetDefaults {
    val AnimationSpec = SpringSpec<Float>()

    val PositionalThreshold = { distance: Float -> distance * 0.2f }

    val VelocityThreshold = { 125.dp.toPixel }
}

enum class CallUiBottomSheetValue {
    HalfExpanded,
    Expanded;

    val draggableSpaceFraction: Float
        get() = when (this) {
            HalfExpanded -> 0.5f
            Expanded -> 1f
        }
}

val Dp.toPixel: Float
    get() = value * Resources.getSystem().displayMetrics.density
