package com.kaleyra.video_sdk.call.callactions.view

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.utils.TextStyleExtensions.clearFontPadding
import kotlin.math.roundToInt

internal object CallActionDefaults {

    val ButtonShape = RoundedCornerShape(18.dp)

    val MinButtonSize = 48.dp

    val ButtonContentPadding = PaddingValues(12.dp)

    val BadgeShape = CircleShape

    val BadgeSize = 20.dp

    val BadgeOffset = 5.dp

    val LabelWidth = MinButtonSize + SheetItemsSpacing

    val ContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHighest

    val ContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val DisabledContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHighest.copy(.38f)

    val DisabledContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface.copy(.38f)

    val CheckedContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.inverseSurface

    val CheckedContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.inverseOnSurface

    val BadgeContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    @Composable
    fun iconButtonColors(
        containerColor: Color = ContainerColor,
        contentColor: Color = ContentColor,
        disabledContainerColor: Color = DisabledContainerColor,
        disabledContentColor: Color = DisabledContentColor,
    ): IconButtonColors =
        IconButtonDefaults.filledIconButtonColors(
            containerColor,
            contentColor,
            disabledContainerColor,
            disabledContentColor
        )

    @Composable
    fun iconToggleButtonColors(
        containerColor: Color = ContainerColor,
        contentColor: Color = ContentColor,
        checkedContainerColor: Color = CheckedContainerColor,
        checkedContentColor: Color = CheckedContentColor,
    ): IconToggleButtonColors = IconButtonDefaults.filledIconToggleButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        checkedContainerColor = checkedContainerColor,
        checkedContentColor = checkedContentColor
    )

    @Composable
    fun badgeColors(
        containerColor: Color = BadgeContainerColor,
        contentColor: Color = contentColorFor(
            BadgeContainerColor
        ),
    ) =
        CardDefaults.cardColors(containerColor, contentColor)

}

@Composable
internal fun CallToggleAction(
    icon: Painter,
    checked: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonText: String? = null,
    buttonContentPadding: PaddingValues = CallActionDefaults.ButtonContentPadding,
    badgePainter: Painter? = null,
    badgeDescription: String? = null,
    badgeText: String? = null,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = contentColorFor(badgeBackgroundColor),
    label: String? = null,
) {
    var isButtonTextDisplayed by remember { mutableStateOf(false) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badgePainter = badgePainter,
        badgeText = badgeText,
        badgeDescription = badgeDescription,
        badgeBackgroundColor = badgeBackgroundColor,
        badgeContentColor = badgeContentColor,
        iconButton = {
            FilledIconToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = CallActionDefaults.MinButtonSize,
                        minHeight = CallActionDefaults.MinButtonSize
                    )
                    .fillMaxWidth(),
                shape = CallActionDefaults.ButtonShape,
                colors = CallActionDefaults.iconToggleButtonColors()
            ) {
                ButtonLayout(
                    icon = icon,
                    text = buttonText,
                    contentDescription = contentDescription,
                    contentPadding = buttonContentPadding,
                    onButtonTextDisplay = { isButtonTextDisplayed = it }
                )
            }
        }
    )
}

@Composable
internal fun CallAction(
    icon: Painter,
    contentDescription: String,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonText: String? = null,
    buttonColor: Color = CallActionDefaults.ContainerColor,
    buttonContentColor: Color = CallActionDefaults.ContentColor,
    disabledButtonColor: Color = CallActionDefaults.DisabledContainerColor,
    disabledButtonContentColor: Color = CallActionDefaults.DisabledContentColor,
    buttonContentPadding: PaddingValues = CallActionDefaults.ButtonContentPadding,
    badgePainter: Painter? = null,
    badgeDescription: String? = null,
    badgeText: String? = null,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = contentColorFor(badgeBackgroundColor),
    label: String? = null,
) {
    var isButtonTextDisplayed by remember { mutableStateOf(false) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badgePainter = badgePainter,
        badgeText = badgeText,
        badgeDescription = badgeDescription,
        badgeBackgroundColor = badgeBackgroundColor,
        badgeContentColor = badgeContentColor,
        iconButton = {
            FilledIconButton(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = CallActionDefaults.MinButtonSize,
                        minHeight = CallActionDefaults.MinButtonSize
                    )
                    .fillMaxWidth(),
                enabled = enabled,
                shape = CallActionDefaults.ButtonShape,
                colors = CallActionDefaults.iconButtonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = disabledButtonColor,
                    disabledContentColor = disabledButtonContentColor
                ),
                onClick = onClick
            ) {
                ButtonLayout(
                    icon = icon,
                    text = buttonText,
                    contentDescription = contentDescription,
                    contentPadding = buttonContentPadding,
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
    label: String? = null,
    badgePainter: Painter? = null,
    badgeText: String? = null,
    badgeDescription: String? = null,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = contentColorFor(badgeBackgroundColor),
) {
    Box(
        modifier = modifier.width(IntrinsicSize.Max),
        contentAlignment = Alignment.TopEnd
    ) {
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
                            val minWidth = CallActionDefaults.LabelWidth.toPx().roundToInt()
                            val maxWidth = constraints.maxWidth.takeIf { it > minWidth } ?: minWidth
                            val placeable = measurable.measure(
                                constraints.copy(
                                    minWidth = minWidth,
                                    maxWidth = maxWidth
                                )
                            )
                            layout(constraints.minWidth, placeable.height) {
                                placeable.placeRelative(-placeable.width / 2, 0)
                            }
                        },
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        val badgeModifier = Modifier.offset {
                with(density) {
                    val offset = CallActionDefaults.BadgeOffset
                        .toPx()
                        .roundToInt()
                    IntOffset(offset, -offset)
                }
            }
        if (badgeText != null) {
            CallActionTextBadge(
                text = badgeText,
                containerColor = badgeBackgroundColor,
                contentColor = badgeContentColor,
                modifier = badgeModifier
            )
        } else if (badgePainter != null) {
            CallActionIconBadge(
                painter = badgePainter,
                contentDescription = badgeDescription,
                containerColor = badgeBackgroundColor,
                contentColor = badgeContentColor,
                modifier = badgeModifier
            )
        }
    }
}

@Composable
private fun ButtonLayout(
    icon: Painter,
    text: String?,
    contentDescription: String,
    contentPadding: PaddingValues,
    onButtonTextDisplay: (isDisplayed: Boolean) -> Unit,
) {
    var shouldDisplayButtonText by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription.takeIf { text == null || !shouldDisplayButtonText }
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
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun CallActionTextBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(containerColor),
) {
    CallActionBadge(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            style = MaterialTheme.typography.labelMedium.clearFontPadding()
        )
    }
}

@Composable
internal fun CallActionIconBadge(
    painter: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(containerColor),
    contentDescription: String? = null
) {
    CallActionBadge(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(
            painter = painter,
            tint = contentColor,
            contentDescription = contentDescription,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp)
        )
    }
}

@Composable
private fun CallActionBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CallActionDefaults.badgeColors(containerColor, contentColor),
        shape = CallActionDefaults.BadgeShape
    ) {
        Box(
            modifier = Modifier
                .size(CallActionDefaults.BadgeSize)
                .align(Alignment.CenterHorizontally)
        ) {
            content()
        }
    }
}

