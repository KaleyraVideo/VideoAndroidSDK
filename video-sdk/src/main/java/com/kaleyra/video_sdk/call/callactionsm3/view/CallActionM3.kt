/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.callactionsm3.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.view.CallActionDefaults
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

const val callActionToggleableTestTag = "callActionToggleableTestTag"

@Immutable
sealed class CallActionM3Configuration {

    abstract val action: CallAction

    @Immutable
    data class Toggleable(override val action: CallAction.Toggleable, val onToggle: (Boolean) -> Unit): CallActionM3Configuration()

    @Immutable
    data class Clickable(override val action: CallAction, val onClick: () -> Unit): CallActionM3Configuration()
}
@Composable
internal fun CallActionM3(
    buttonWidth: Dp,
    containerWidth: Dp,
    badgeDisplayText: String? = null,
    configuration: CallActionM3Configuration,
    isDarkTheme: Boolean = false,
    displayLabel: Boolean = false
) {
    val content =  @Composable {
        when (configuration) {
            is CallActionM3Configuration.Toggleable ->
                ToggleableCallActionM3(
                    buttonWidth = buttonWidth,
                    action = configuration.action,
                    displayInnerLabel = displayLabel,
                    onToggle = configuration.onToggle,
                    isDarkTheme = isDarkTheme)
            is CallActionM3Configuration.Clickable ->
                ClickableCallActionM3(
                    buttonWidth = buttonWidth,
                    action = configuration.action,
                    displayInnerLabel = displayLabel,
                    onClick = configuration.onClick,
                    isDarkTheme = isDarkTheme)
        }
    }
    val badgedContent = @Composable {
        if (badgeDisplayText == null) content()
        else Badge(displayText = badgeDisplayText, content = content)
    }

    if (displayLabel && buttonWidth <= CallActionM3Defaults.Size) {
        Column(
            modifier = Modifier.width(containerWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            badgedContent()
            Text(
                text = textFor(configuration.action),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp,
                fontSize = 11.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier.width(containerWidth),
            contentAlignment = Alignment.Center,
        ) {
            badgedContent()
        }
    }
}

@Composable
internal fun ClickableCallActionM3(
    buttonWidth: Dp,
    action: CallAction,
    displayInnerLabel: Boolean = buttonWidth > CallActionM3Defaults.Size,
    onClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = colorsFor(action, isDarkTheme)
    val toggled by remember(action) {
        derivedStateOf {
            action is CallAction.Toggleable && action.isToggled
        }
    }

    FilledIconButton(
        modifier = Modifier
            .padding(2.dp)
            .width(buttonWidth.minus(if (buttonWidth > CallActionM3Defaults.Size) 8.dp else 0.dp))
            .height(CallActionM3Defaults.Size)
            .defaultMinSize(minWidth = CallActionM3Defaults.Size),
        enabled = action.isEnabled,
        onClick = onClick,
        shape = RoundedCornerShape(CallActionM3Defaults.CornerRadius),
        content = {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(CallActionM3Defaults.IconSize),
                    painter = painterFor(action),
                    contentDescription = descriptionFor(action),
                )
                if (!displayInnerLabel) return@Row
                if (buttonWidth <= CallActionM3Defaults.Size) return@Row
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = descriptionFor(action),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = colors.backgroundColor(enabled = action.isEnabled).value,
            contentColor = colors.contentColor(toggled = toggled, enabled = action.isEnabled).value,
            disabledContainerColor = colors.backgroundColor(enabled = action.isEnabled).value,
            disabledContentColor = colors.contentColor(toggled = toggled, enabled = action.isEnabled).value
        )
    )
}

@Composable
internal fun ToggleableCallActionM3(
    buttonWidth: Dp,
    action: CallAction.Toggleable,
    displayInnerLabel: Boolean = buttonWidth > CallActionM3Defaults.Size,
    onToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = colorsFor(action, isDarkTheme)
    val toggled by remember(action) {
        derivedStateOf {
            action.isToggled
        }
    }
    FilledIconToggleButton(
        modifier = Modifier
            .padding(2.dp)
            .height(CallActionM3Defaults.Size)
            .width(buttonWidth.minus(if (buttonWidth > CallActionM3Defaults.Size) 8.dp else 0.dp))
            .testTag(callActionToggleableTestTag),
        enabled = action.isEnabled,
        checked = action.isToggled,
        onCheckedChange = { checked ->
            onToggle(checked)
        },
        shape = RoundedCornerShape(CallActionM3Defaults.CornerRadius),
        content = {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(CallActionM3Defaults.IconSize),
                    painter = painterFor(action),
                    contentDescription = descriptionFor(action),
                )
                if (!displayInnerLabel) return@Row
                if (buttonWidth <= CallActionM3Defaults.Size) return@Row
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = descriptionFor(action),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        colors = IconButtonDefaults.iconToggleButtonColors(
            containerColor = colors.toggledBackgroundColor(toggled = toggled, enabled = action.isEnabled).value,
            contentColor = colors.contentColor(toggled = toggled, enabled = action.isEnabled).value,
            checkedContainerColor = colors.toggledBackgroundColor(toggled = toggled, enabled = action.isEnabled).value,
            checkedContentColor = colors.contentColor(toggled = toggled, enabled = action.isEnabled).value,
            disabledContainerColor = colors.toggledBackgroundColor(toggled = toggled, enabled = action.isEnabled).value,
            disabledContentColor = colors.contentColor(toggled = toggled, enabled = action.isEnabled).value
        )
    )
}

@Composable
internal fun CallActionLabelM3(
    buttonWidth: Dp,
    containerWidth: Dp,
    badgeDisplayText: String? = null,
    action: CallAction,
    onClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = colorsFor(action, isDarkTheme)

    val textWidth = remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .width(containerWidth),
        contentAlignment = Alignment.Center
    ) {
        val button = @Composable {
            TextButton(
                modifier = Modifier
                    .padding(2.dp)
                    .indication(
                        interactionSource = interactionSource,
                        indication = rememberRipple(bounded = false, radius = CallActionDefaults.RippleRadius)
                    )
                    .height(48.dp)
                    .width(buttonWidth)
                    .fillMaxWidth(),
                onClick = onClick,
                shape = RoundedCornerShape(CallActionM3Defaults.CornerRadius),
                contentPadding = PaddingValues(0.dp),
                content = {
                    Text(
                        modifier = Modifier
                            .onSizeChanged {
                                textWidth.value = it.width
                            }
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        text = textFor(action = action),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.contentColor(toggled = action.isEnabled, enabled = action.isEnabled).value,
                    )
                },
                enabled = action.isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.backgroundColor(enabled = action.isEnabled).value,
                    contentColor = colors.contentColor(toggled = action.isEnabled, enabled = action.isEnabled).value,
                    disabledContainerColor = colors.backgroundColor(enabled = action.isEnabled).value,
                    disabledContentColor = colors.contentColor(toggled = action.isEnabled, enabled = action.isEnabled).value
                )
            )
        }
        if (badgeDisplayText != null) Badge(displayText = badgeDisplayText, content = button)
        else button()
    }
}

@Composable
fun OrientationAwareComponent(
    portraitContent: @Composable () -> Unit,
    landscapeContent: @Composable () -> Unit
) {
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> landscapeContent()
        else -> portraitContent()
    }

}

@Composable
fun Badge(
    displayText: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier
        .wrapContentSize()) {
        Box {
            content()
        }
        Card(
            modifier = Modifier
                .defaultMinSize(12.dp, 12.dp)
                .align(Alignment.TopEnd),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = CircleShape,
        ) {
            Text(
                modifier = Modifier
                    .padding(2.dp)
                    .defaultMinSize(14.dp, 14.dp),
                text = displayText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CallActionFor(
    buttonWidth: Dp,
    containerWidth: Dp,
    actionConfiguration: CallActionM3Configuration,
    badgeDisplayText: String? = null,
    displayLabel: Boolean = false,
    isDarkTheme: Boolean) {
    val callAction = @Composable {
        when (actionConfiguration.action) {
            is CallAction.Camera,
            is CallAction.Microphone,
            is CallAction.VirtualBackground,
            is CallAction.More,
            is CallAction.ScreenShare -> {
                CallActionM3(
                    buttonWidth = buttonWidth,
                    containerWidth = containerWidth,
                    configuration = actionConfiguration,
                    displayLabel = displayLabel,
                    badgeDisplayText = badgeDisplayText,
                    isDarkTheme = isDarkTheme)
            }
            is CallAction.Answer -> {
                actionConfiguration as CallActionM3Configuration.Clickable
                OrientationAwareComponent(
                    portraitContent = {
                        CallActionLabelM3(
                            buttonWidth = buttonWidth,
                            containerWidth = containerWidth,
                            action = actionConfiguration.action,
                            onClick = actionConfiguration.onClick,
                            badgeDisplayText = badgeDisplayText,
                            isDarkTheme = isDarkTheme,
                        )
                    },
                    landscapeContent = {
                        CallActionM3(
                            buttonWidth = buttonWidth,
                            containerWidth = containerWidth,
                            configuration = actionConfiguration,
                            isDarkTheme = isDarkTheme,
                            badgeDisplayText = badgeDisplayText,
                            displayLabel = displayLabel
                        )
                    }
                )
            }
            is CallAction.Audio,
            is CallAction.Chat,
            is CallAction.FileShare,
            is CallAction.HangUp,
            is CallAction.SwitchCamera,
            is CallAction.Whiteboard -> {
                CallActionM3(
                    buttonWidth = buttonWidth,
                    containerWidth = containerWidth,
                    configuration = actionConfiguration,
                    displayLabel = displayLabel)
            }
        }
    }
    callAction()
}

@Preview(name = "Light Mode")
@Composable
internal fun CallActionPreview() = KaleyraM3Theme {
    CallActionToggleablePreview(CallAction.Microphone(isToggled = false, isEnabled = true), false)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionPreviewDark() = KaleyraM3Theme {
    CallActionToggleablePreview(CallAction.Microphone(isToggled = false, isEnabled = true), true)
}

@Preview(name = "Light Mode")
@Composable
internal fun CallActionDisabledPreview() {
    CallActionToggleablePreview(CallAction.Microphone(isToggled = false, isEnabled = false),false)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionDisabledPreviewDark() {
    CallActionToggleablePreview(CallAction.Microphone(isToggled = false, isEnabled = false), true)
}

@Preview(name = "Light Mode")
@Composable
internal fun CallActionHangUpPreview() {
    CallActionPreview(CallAction.HangUp(isEnabled = true), false)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionHangUpPreviewDark() {
    CallActionPreview(CallAction.HangUp(isEnabled = true), true)
}

@Preview(name = "Light Mode")
@Composable
internal fun CallActionAudioUpPreview() {
    CallActionPreview(CallAction.Audio(isEnabled = true), false)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionAudioPreviewDark() {
    CallActionPreview(CallAction.Audio(isEnabled = true), true)
}

@Preview(name = "Portarit Light Mode", showBackground = true)
@Composable
internal fun AnswerButtonPortraitPreview() {
    OrientationAwareCallActionPreview(CallAction.Answer(isEnabled = true), false)
}

@Preview(name = "Portrait Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
internal fun AnswerButtonPortraitDarkPreview() {
    OrientationAwareCallActionPreview(CallAction.Answer(isEnabled = true), true)
}

@Preview(name = "Landscape Light Mode", showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 640)
@Composable
internal fun AnswerButtonLandscapePreview() {
    OrientationAwareCallActionPreview(CallAction.Answer(isEnabled = true), false)
}

@Preview(name = "Landscape Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 640)
@Composable
internal fun AnswerButtonDarkLandscapePreview() {
    OrientationAwareCallActionPreview(CallAction.Answer(isEnabled = true), true)
}

@Composable
private fun OrientationAwareCallActionPreview(action: CallAction, isDarkTheme: Boolean) {
    KaleyraM3Theme {
        Column(modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
            OrientationAwareComponent(
                portraitContent = {
                    CallActionM3(
                        buttonWidth = 48.dp,
                        containerWidth = 96.dp,
                        badgeDisplayText = "2",
                        configuration = CallActionM3Configuration.Clickable(CallAction.Answer(), {}),
                        isDarkTheme = isDarkTheme,
                        displayLabel = false
                    )
                },
                landscapeContent = {
                    CallActionLabelM3(
                        buttonWidth = 150.dp,
                        containerWidth = 400.dp,
                        action = action,
                        onClick = {},
                        badgeDisplayText = "3",
                        isDarkTheme = isDarkTheme
                    )
                }
            )
        }
    }
}

@Composable
private fun CallActionPreview(action: CallAction, isDarkTheme: Boolean) {
    KaleyraM3Theme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            CallActionM3(
                buttonWidth = 48.dp,
                containerWidth = 96.dp,
                badgeDisplayText = "7",
                configuration = CallActionM3Configuration.Clickable(action, {}),
                isDarkTheme = isDarkTheme
            )
        }
    }
}


@Composable
private fun CallActionToggleablePreview(action: CallAction.Toggleable, isDarkTheme: Boolean) {
    KaleyraM3Theme {
        val mutableAction = remember { mutableStateOf(action) }
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            CallActionM3(
                buttonWidth = 48.dp,
                containerWidth = 96.dp,
                badgeDisplayText = if (action is CallAction.Microphone) "!" else null,
                configuration = CallActionM3Configuration.Toggleable(mutableAction.value) {
                    mutableAction.value = mutableAction.value.copy(it)
                },
                displayLabel = true,
                isDarkTheme = isDarkTheme
            )
        }
    }
}
