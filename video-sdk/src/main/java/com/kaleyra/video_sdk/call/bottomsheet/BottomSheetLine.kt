package com.kaleyra.video_sdk.call.bottomsheet

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.theme.KaleyraTheme
import java.io.Serializable

internal const val LineTag = "LineTag"
internal val ExpandedLineWidth = 28.dp
internal val CollapsedLineWidth = 4.dp

private val LineHeight = 4.dp

// Serializable is needed to save the line state in {@link BottomSheetContentState#Saver}
@Immutable
internal sealed class LineState : Serializable {

    @Immutable
    data object Expanded : LineState()

    // Argb int color to make the parameter serializable
    @Immutable
    data class Collapsed(val argbColor: Int? = null) : LineState()
}

@Composable
internal fun Line(
    state: LineState,
    onClickLabel: String,
    onClick: () -> Unit
) {
    val contentColor = LocalContentColor.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val width by animateDpAsState(targetValue = if (state is LineState.Collapsed) CollapsedLineWidth else ExpandedLineWidth)
        val color = if (state is LineState.Collapsed && state.argbColor != null) Color(state.argbColor) else contentColor.copy(alpha = 0.6f)

        Spacer(
            modifier = Modifier
                .size(width, LineHeight)
                .background(
                    color = color,
                    shape = CircleShape
                )
                .testTag(LineTag)
        )
    }
}

@Preview
@Composable
internal fun CollapsedLineNoBackgroundPreview() {
    KaleyraTheme {
        Line(state = LineState.Collapsed(Color.White.toArgb()), onClickLabel = "onClickLabel", onClick = { })
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CollapsedLinePreview() {
    KaleyraTheme {
        Surface {
            Line(state = LineState.Collapsed(Color.White.toArgb()), onClickLabel = "onClickLabel", onClick = { })
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ExpandedLinePreview() {
    KaleyraTheme {
        Surface {
            Line(state = LineState.Expanded, onClickLabel = "onClickLabel", onClick = { })
        }
    }
}