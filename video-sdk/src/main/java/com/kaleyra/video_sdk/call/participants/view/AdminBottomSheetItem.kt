package com.kaleyra.video_sdk.call.participants.view

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun AdminBottomSheetItem(
    text: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    color: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RectangleShape,
        interactionSource = interactionSource,
        colors =  ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = color.copy(alpha = .38f),
        ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
        modifier = modifier
    ) {
        Icon(
            painter = painter,
            modifier = Modifier.size(24.dp),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(26.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}