package com.kaleyra.video_sdk.call.callactionsnew

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

object CallActionDefaults {

    val buttonShape = RoundedCornerShape(18.dp)

    val minButtonSize = 48.dp

    val buttonContentPadding = PaddingValues(12.dp)

    val badgeShape = CircleShape

    val badgeSize = 20.dp

    val badgeOffset = 4.dp

    @Composable
    fun iconButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.onSurface.copy(.1f)
    ): IconButtonColors =
        IconButtonDefaults.filledIconButtonColors(containerColor)

    @Composable
    fun iconToggleButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.onSurface.copy(.1f),
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        checkedContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isSystemInDarkTheme()) 1f else .66f),
        checkedContentColor: Color = MaterialTheme.colorScheme.surface,
    ): IconToggleButtonColors = IconButtonDefaults.filledIconToggleButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        checkedContainerColor = checkedContainerColor,
        checkedContentColor = checkedContentColor
    )

    @Composable
    fun badgeColors(containerColor: Color = MaterialTheme.colorScheme.primary) =
        CardDefaults.cardColors(containerColor)

}

@Composable
fun CallToggleAction(
    icon: Painter,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    label: String? = null,
    contentPadding: PaddingValues = CallActionDefaults.buttonContentPadding,
    badge: (@Composable BoxScope.() -> Unit)? = null
) {
    var isButtonTextDisplayed by remember { mutableStateOf(true) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badge = badge,
        iconButton = {
            FilledIconToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = CallActionDefaults.minButtonSize,
                        minHeight = CallActionDefaults.minButtonSize
                    )
                    .fillMaxWidth(),
                shape = CallActionDefaults.buttonShape,
                colors = CallActionDefaults.iconToggleButtonColors()
            ) {
                ButtonLayout(
                    icon = icon,
                    text = buttonText,
                    contentPadding = contentPadding,
                    onButtonTextDisplay = { isButtonTextDisplayed = it }
                )
            }
        }
    )
}

@Composable
fun CallAction(
    icon: Painter,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    label: String? = null,
    contentPadding: PaddingValues = CallActionDefaults.buttonContentPadding,
    badge: (@Composable BoxScope.() -> Unit)? = null
) {
    var isButtonTextDisplayed by remember { mutableStateOf(true) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badge = badge,
        iconButton = {
            FilledIconButton(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = CallActionDefaults.minButtonSize,
                        minHeight = CallActionDefaults.minButtonSize
                    )
                    .fillMaxWidth(),
                shape = CallActionDefaults.buttonShape,
                colors = CallActionDefaults.iconButtonColors(),
                onClick = onClick
            ) {
                ButtonLayout(
                    icon = icon,
                    text = buttonText,
                    contentPadding = contentPadding,
                    onButtonTextDisplay = { isButtonTextDisplayed = it }
                )
            }
        }
    )
}

@Composable
private fun CallActionLayout(
    modifier: Modifier,
    iconButton: @Composable () -> Unit,
    badge: (@Composable BoxScope.() -> Unit)?,
    label: String? = null
) {
    Box(modifier.width(IntrinsicSize.Max)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            iconButton()
            if (label != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
                            layout(constraints.minWidth, placeable.height) {
                                placeable.placeRelative(-placeable.width / 2, 0)
                            }
                        },
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (badge != null) {
            Badge(
                modifier = modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        with(density) {
                            val offset = CallActionDefaults.badgeOffset
                                .toPx()
                                .roundToInt()
                            IntOffset(offset, -offset)
                        }
                    },
                content = badge
            )
        }
    }
}

@Composable
private fun ButtonLayout(
    icon: Painter,
    text: String?,
    contentPadding: PaddingValues,
    onButtonTextDisplay: (isDisplayed: Boolean) -> Unit
) {
    var shouldDisplayButtonText by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            // TODO set content description
            contentDescription = null
        )
        if (text != null && shouldDisplayButtonText) {
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
                        shouldDisplayButtonText = placeable.width <= constraints.maxWidth
                        onButtonTextDisplay(shouldDisplayButtonText)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    },
                text = text
            )
        }
    }
}

@Composable
private fun Badge(
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
) {
    Card(
        modifier = modifier,
        colors = CallActionDefaults.badgeColors(),
        shape = CallActionDefaults.badgeShape
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(CallActionDefaults.badgeSize)
                .align(Alignment.CenterHorizontally),
            content = content
        )
    }
}

