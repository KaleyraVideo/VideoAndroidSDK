package com.kaleyra.video_sdk.call.callscreenscaffold

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
internal object CallScreenScaffoldDefaults {

    val PaddingValues = PaddingValues(12.dp)
}

@Composable
internal fun Scrim(
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

internal fun Modifier.dragHandleSemantics(
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