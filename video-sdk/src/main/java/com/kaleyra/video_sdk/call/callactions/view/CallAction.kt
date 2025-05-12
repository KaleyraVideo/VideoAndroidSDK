package com.kaleyra.video_sdk.call.callactions.view

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.utils.TextStyleExtensions.clearFontPadding
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.extensions.DpExtensions.toPixel
import com.kaleyra.video_sdk.extensions.ModifierExtensions.drawRoundedCornerBorder
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlin.math.roundToInt

internal object CallActionDefaults {

    val ButtonShape = RoundedCornerShape(18.dp)

    val MinButtonSize = 48.dp

    val ButtonContentPadding = PaddingValues(12.dp)

    val BadgeShape = CircleShape

    val BadgeSize = 16.dp

    val BadgeHorizontalOffset = 0.dp

    val BadgeVerticalOffset = 7.dp

    val BadgeExtendedHorizontalOffset = 8.dp

    val BadgeExtendedVerticalOffset = 7.dp

    val LabelWidth = MinButtonSize + SheetItemsSpacing

    val ContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHighest

    val ContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

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

    val DisabledContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = ContainerColor.copy(.38f)

    val DisabledContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = ContentColor.copy(.38f)

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
        disabledContainerColor: Color = DisabledContainerColor,
        disabledContentColor: Color = DisabledContentColor,
    ): IconToggleButtonColors = IconButtonDefaults.filledIconToggleButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        checkedContainerColor = checkedContainerColor,
        checkedContentColor = checkedContentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
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
    badgeCount: Int = 0,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = contentColorFor(badgeBackgroundColor),
    label: String? = null,
) {
    var isButtonTextDisplayed by remember { mutableStateOf(false) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badgePainter = badgePainter,
        badgeCount = badgeCount,
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
    badgeCount: Int = 0,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = contentColorFor(badgeBackgroundColor),
    label: String? = null,
) {
    var isButtonTextDisplayed by remember { mutableStateOf(false) }
    CallActionLayout(
        modifier = modifier,
        label = label.takeIf { !isButtonTextDisplayed },
        badgePainter = badgePainter,
        badgeCount = badgeCount,
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
    badgeCount: Int = 0,
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
        val extendedBadgeModifier = Modifier.offset {
            with(density) {
                val horizontalOffset = CallActionDefaults.BadgeExtendedHorizontalOffset
                    .toPx()
                    .roundToInt()
                val verticalOffset = CallActionDefaults.BadgeExtendedVerticalOffset
                    .toPx()
                    .roundToInt()
                IntOffset(horizontalOffset, -verticalOffset)
            }
        }
        val badgeModifier = Modifier.offset {
            with(density) {
                val horizontalOffset = CallActionDefaults.BadgeHorizontalOffset
                    .toPx()
                    .roundToInt()
                val verticalOffset = CallActionDefaults.BadgeVerticalOffset
                    .toPx()
                    .roundToInt()
                IntOffset(horizontalOffset, -verticalOffset)
            }
        }
        if (badgeCount != 0) {
            CallActionBadgeCount(
                count = badgeCount,
                containerColor = badgeBackgroundColor,
                contentColor = badgeContentColor,
                modifier = if (badgeCount > 99) extendedBadgeModifier else badgeModifier
            )
        } else if (badgePainter != null) {
            CallActionBadgeIcon(
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
internal fun CallActionBadgeCount(
    count: Int,
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
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp),
            text = when {
                count > 99 -> stringResource(R.string.kaleyra_call_badge_count_overflow)
                else -> count.toString()
            },
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.clearFontPadding()
        )
    }
}

@Composable
internal fun CallActionBadgeIcon(
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
                .padding(2.dp)
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
    val strokeColor = MaterialTheme.colorScheme.surfaceContainer
    Card(
        modifier = modifier
            .drawRoundedCornerBorder(
                width = 1.5.dp,
                color = strokeColor,
                alpha = 1f,
                cornerRadius =  CornerRadius(50.dp.toPixel)
            ),
        colors = CallActionDefaults.badgeColors(containerColor, contentColor),
        shape = CallActionDefaults.BadgeShape
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(CallActionDefaults.BadgeSize)
                .height(CallActionDefaults.BadgeSize)
                .align(Alignment.CenterHorizontally)
        ) {
            content()
        }
    }
}

@MultiConfigPreview
@Composable
internal fun BadgeCountMoreThan99Preview() {
    KaleyraTheme {
        Surface(modifier = Modifier.background(color = Color.Yellow)) {
            CallActionBadgeCount(count = 199, modifier = Modifier.background(color = Color.Yellow).padding(12.dp))
        }
    }
}

@MultiConfigPreview
@Composable
internal fun BadgeCount99Preview() {
    KaleyraTheme {
        Surface(modifier = Modifier.background(color = Color.Yellow)) {
            CallActionBadgeCount(count = 99, modifier = Modifier.background(color = Color.Yellow).padding(12.dp))
        }
    }
}

@MultiConfigPreview
@Composable
internal fun BadgeCountLessThan10Preview() {
    KaleyraTheme {
        Surface(modifier = Modifier.background(color = Color.Yellow)) {
            CallActionBadgeCount(count = 9, modifier = Modifier.background(color = Color.Yellow).padding(12.dp))
        }
    }
}

@MultiConfigPreview
@Composable
internal fun CallActionPreview() {
    KaleyraTheme {
        Surface(modifier = Modifier.size(150.dp)) {
            ChatAction({}, badgeCount = 199, modifier = Modifier.background(color = Color.Blue).padding(10.dp))
        }
    }
}

@MultiConfigPreview
@Composable
internal fun CallActionBadge99Preview() {
    KaleyraTheme {
        Surface(modifier = Modifier.size(150.dp)) {
            ChatAction({}, badgeCount = 99, modifier = Modifier.background(color = Color.Blue).padding(10.dp))
        }
    }
}

@MultiConfigPreview
@Composable
internal fun CallActionBadge3Preview() {
    KaleyraTheme {
        Surface(modifier = Modifier.size(150.dp)) {
            ChatAction({}, badgeCount = 3, modifier = Modifier.background(color = Color.Blue).padding(10.dp))
        }
    }
}

@MultiConfigPreview
@Composable
internal fun CallActionIconBadgePreview() {
    KaleyraTheme {
        Surface(modifier = Modifier.size(150.dp)) {
            MicAction(
                modifier = Modifier.background(color = Color.Blue).padding(10.dp),
                checked = false,
                onCheckedChange = {},
                error = true
            )
        }
    }
}