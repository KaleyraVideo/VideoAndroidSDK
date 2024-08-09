import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.extensions.ModifierExtensions.supportRtl

private val MinSize = 48.dp
private val IconSize = 24.dp

@Composable
internal fun IconButton(
    icon: Painter,
    iconDescription: String?,
    enabledIconTint: Color = LocalContentColor.current,
    disabledIconTint: Color = LocalContentColor.current,
    iconSize: Dp = IconSize,
    enabled: Boolean = true,
    supportRtl: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = rememberRipple(bounded = false, radius = IconSize),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .highlightOnFocus(interactionSource)
            .defaultMinSize(minWidth = MinSize, minHeight = MinSize)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = indication
            ),
        contentAlignment = Alignment.Center
    ) {
        val tint = if (enabled) enabledIconTint else disabledIconTint
        val mod = Modifier
            .size(iconSize)
            .then(if (supportRtl) Modifier.supportRtl() else Modifier)
        Icon(
            painter = icon,
            tint = tint,
            contentDescription = iconDescription,
            modifier = mod
        )
    }
}