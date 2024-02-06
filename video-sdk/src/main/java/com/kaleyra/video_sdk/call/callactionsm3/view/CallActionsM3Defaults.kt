package com.kaleyra.video_sdk.call.callactionsm3.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.theme.disabledAlpha
import com.kaleyra.video_sdk.theme.kaleyra_answer_dark_color
import com.kaleyra.video_sdk.theme.kaleyra_answer_light_color
import com.kaleyra.video_sdk.theme.kaleyra_hang_up_dark_color
import com.kaleyra.video_sdk.theme.kaleyra_hang_up_light_color

@Stable
internal interface CallActionColorsM3 {
    @Composable
    fun toggledBackgroundColor(toggled: Boolean, enabled: Boolean): State<Color>

    @Composable
    fun backgroundColor(enabled: Boolean): State<Color>

    @Composable
    fun contentColor(toggled: Boolean, enabled: Boolean): State<Color>
}

internal object CallActionM3Defaults {

    val Size = 48.dp

    val IconSize = 24.dp

    val CornerRadius = Size / 2.8f

    private val disabledBackgroundAlpha = 0.12f

    @Composable
    fun colors(
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        iconColor: Color = contentColorFor(backgroundColor),
        textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledBackgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledBackgroundAlpha),
        disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
        disabledTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = disabledAlpha),
        toggledBackgroundColor: Color = MaterialTheme.colorScheme.primary,
        toggledIconColor: Color = if (toggledBackgroundColor.luminance() > .5f) Color.Black else Color.White
    ): CallActionColorsM3 = DefaultM3Colors(
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        textColor = textColor,
        disabledBackgroundColor = disabledBackgroundColor,
        disabledIconColor = disabledIconColor,
        disabledTextColor = disabledTextColor,
        toggledBackgroundColor = toggledBackgroundColor,
        toggledIconColor = toggledIconColor
    )

    sealed class Label {
        data object Never
        data object Inside
        data object Below
    }
}

@Immutable
private class DefaultM3Colors(
    private val backgroundColor: Color,
    private val iconColor: Color,
    private val textColor: Color,
    private val disabledBackgroundColor: Color,
    private val disabledIconColor: Color,
    private val disabledTextColor: Color,
    private val toggledBackgroundColor: Color,
    private val toggledIconColor: Color,
) : CallActionColorsM3 {
    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> {
        val color = if (enabled) backgroundColor else disabledBackgroundColor
        return rememberUpdatedState(color)
    }

    @Composable
    override fun toggledBackgroundColor(toggled: Boolean, enabled: Boolean): State<Color> {
        val color = when {
            !enabled -> disabledBackgroundColor
            !toggled -> backgroundColor
            else -> toggledBackgroundColor
        }
        return rememberUpdatedState(color)
    }

    @Composable
    override fun contentColor(toggled: Boolean, enabled: Boolean): State<Color> {
        val color = when {
            !enabled -> disabledIconColor
            !toggled -> iconColor
            else -> toggledIconColor
        }
        return rememberUpdatedState(color)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultM3Colors

        if (backgroundColor != other.backgroundColor) return false
        if (iconColor != other.iconColor) return false
        if (textColor != other.textColor) return false
        if (disabledBackgroundColor != other.disabledBackgroundColor) return false
        if (disabledIconColor != other.disabledIconColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (toggledBackgroundColor != other.toggledBackgroundColor) return false
        if (toggledIconColor != other.toggledIconColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundColor.hashCode()
        result = 31 * result + iconColor.hashCode()
        result = 31 * result + textColor.hashCode()
        result = 31 * result + disabledBackgroundColor.hashCode()
        result = 31 * result + disabledIconColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()
        result = 31 * result + toggledBackgroundColor.hashCode()
        result = 31 * result + toggledIconColor.hashCode()
        return result
    }
}

@Composable
internal fun textFor(action: CallAction): String = stringResource(
    id = when (action) {
        is CallAction.Camera -> R.string.kaleyra_call_action_video_disable
        is CallAction.Microphone -> R.string.kaleyra_call_action_mic_mute
        is CallAction.SwitchCamera -> R.string.kaleyra_call_action_switch_camera
        is CallAction.HangUp -> R.string.kaleyra_call_hangup
        is CallAction.Answer -> R.string.kaleyra_call_answer
        is CallAction.Chat -> R.string.kaleyra_call_action_chat
        is CallAction.Whiteboard -> R.string.kaleyra_call_action_whiteboard
        is CallAction.FileShare -> R.string.kaleyra_call_action_file_share
        is CallAction.Audio -> R.string.kaleyra_call_action_audio_route
        is CallAction.ScreenShare -> R.string.kaleyra_call_action_screen_share
        is CallAction.VirtualBackground -> R.string.kaleyra_call_action_virtual_background
        is CallAction.More -> R.string.kaleyra_call_action_more
    }
)

@Composable
internal fun descriptionFor(action: CallAction) = stringResource(
    id = when (action) {
        is CallAction.Camera -> R.string.kaleyra_call_action_disable_camera_description
        is CallAction.Microphone -> R.string.kaleyra_call_action_disable_mic_description
        is CallAction.SwitchCamera -> R.string.kaleyra_call_action_switch_camera_description
        is CallAction.HangUp -> R.string.kaleyra_call_hangup
        is CallAction.Answer -> R.string.kaleyra_call_answer
        is CallAction.Chat -> R.string.kaleyra_call_action_chat
        is CallAction.Whiteboard -> R.string.kaleyra_call_action_whiteboard
        is CallAction.FileShare -> R.string.kaleyra_call_action_file_share
        is CallAction.Audio -> R.string.kaleyra_call_action_audio_route
        is CallAction.ScreenShare -> R.string.kaleyra_call_action_screen_share
        is CallAction.VirtualBackground -> R.string.kaleyra_call_action_virtual_background
        is CallAction.More -> R.string.kaleyra_call_action_more
    }
)

@Composable
internal fun painterFor(action: CallAction): Painter = painterResource(
    id = when (action) {
        is CallAction.Camera -> R.drawable.ic_kaleyra_camera_off
        is CallAction.Microphone -> R.drawable.ic_kaleyra_mic_off
        is CallAction.SwitchCamera -> R.drawable.ic_kaleyra_switch_camera
        is CallAction.HangUp -> R.drawable.ic_kaleyra_hangup
        is CallAction.Answer -> R.drawable.ic_kaleyra_answer
        is CallAction.Chat -> R.drawable.ic_kaleyra_chat
        is CallAction.Whiteboard -> R.drawable.ic_kaleyra_whiteboard
        is CallAction.FileShare -> R.drawable.ic_kaleyra_file_share
        is CallAction.Audio -> {
            when (action.device) {
                AudioDeviceUi.LoudSpeaker -> R.drawable.ic_kaleyra_loud_speaker
                AudioDeviceUi.WiredHeadset -> R.drawable.ic_kaleyra_wired_headset
                AudioDeviceUi.EarPiece -> R.drawable.ic_kaleyra_earpiece
                AudioDeviceUi.Muted -> R.drawable.ic_kaleyra_muted
                is AudioDeviceUi.Bluetooth -> R.drawable.ic_kaleyra_bluetooth_headset
            }
        }

        is CallAction.ScreenShare -> R.drawable.ic_kaleyra_screen_share
        is CallAction.VirtualBackground -> R.drawable.ic_kaleyra_virtual_background
        is CallAction.More -> R.drawable.ic_kaleyra_more
    }
)

@Composable
internal fun colorsFor(action: CallAction, isDarkTheme: Boolean): CallActionColorsM3 {
    return when (action) {
        is CallAction.HangUp -> {
            val backgroundColor = if (isDarkTheme) kaleyra_hang_up_dark_color else kaleyra_hang_up_light_color
            CallActionM3Defaults.colors(
                backgroundColor = backgroundColor,
                iconColor = Color.White,
                disabledBackgroundColor = backgroundColor.copy(alpha = disabledAlpha),
                disabledIconColor = Color.White.copy(alpha = disabledAlpha)
            )
        }

        is CallAction.Answer -> {
            val backgroundColor = if (isDarkTheme) kaleyra_answer_dark_color else kaleyra_answer_light_color
            CallActionM3Defaults.colors(
                backgroundColor = backgroundColor,
                iconColor = Color.White,
                textColor = Color.White,
                toggledIconColor = Color.White
            )
        }

        else -> CallActionM3Defaults.colors()
    }
}
