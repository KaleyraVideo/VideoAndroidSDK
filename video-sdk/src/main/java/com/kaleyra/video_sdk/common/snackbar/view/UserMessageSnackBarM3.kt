@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Stable
data class UserMessageSnackbarActionConfig(
    val action: TextRef,
    val iconResource: Int? = null,
    val onActionClick: () -> Unit
)

@Composable
internal fun UserMessageSnackbarM3(
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    iconTint: Color = MaterialTheme.colorScheme.surface,
    message: String,
    actionConfig: UserMessageSnackbarActionConfig? = null,
    onDismissClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.surface
) {
    var isMultilineMessage by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .focusRequester(focusRequester)
    ) {
        Row(modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = if (actionConfig != null || isMultilineMessage) RoundedCornerShape(16.dp) else CircleShape
            )
            .align(Alignment.Center)) {
            if (actionConfig == null && iconPainter != null) {
                Spacer(Modifier.size(8.dp))
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically),
                    painter = iconPainter,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Text(
                color = contentColor,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = 32.dp)
                    .padding(top = 4.dp, bottom = 4.dp, start = if (actionConfig != null || iconPainter == null) 16.dp else 8.dp, end = if (onDismissClick == null && actionConfig == null) 16.dp else 8.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .align(Alignment.CenterVertically)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                    },
                text = message,
                style = MaterialTheme.typography.bodySmall,
                onTextLayout = { textLayoutResult ->
                    isMultilineMessage = textLayoutResult.lineCount > 1
                })
            val iconButtonInteractionSource = remember { MutableInteractionSource() }
            if (actionConfig == null && onDismissClick != null) {
                IconButton(
                    modifier = Modifier
                        .padding(end = 8.dp, start = 0.dp)
                        .align(Alignment.CenterVertically)
                        .highlightOnFocus(iconButtonInteractionSource)
                        .size(24.dp),
                    interactionSource = iconButtonInteractionSource,
                    onClick = onDismissClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_snackbar_close),
                        tint = contentColor,
                        contentDescription = stringResource(id = R.string.kaleyra_close))
                }
            }
            val filledButtonInteractionSource = remember { MutableInteractionSource() }
            if (actionConfig !== null) FilledTonalButton(
                modifier = Modifier
                    .height(40.dp)
                    .padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                    .highlightOnFocus(filledButtonInteractionSource)
                    .align(Alignment.CenterVertically),
                contentPadding = PaddingValues(horizontal = 8.dp),
                interactionSource = filledButtonInteractionSource,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = contentColor),
                onClick = actionConfig.onActionClick) {
                val actionDescription = actionConfig.action.resolve(LocalContext.current)
                if (actionConfig.iconResource != null) {
                    Icon(
                        tint = backgroundColor,
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp),
                        painter = painterResource(id = actionConfig.iconResource),
                        contentDescription = actionDescription)
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    maxLines = 1,
                    text = actionDescription,
                    color = backgroundColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
internal fun UserMessageInfoSnackbarM3(
    message: String, onDismissClick: (() -> Unit)? = null,
    iconTint: Color =  MaterialTheme.colorScheme.surface,
    actionConfig: UserMessageSnackbarActionConfig? = null) {
    UserMessageSnackbarM3(
        iconTint = iconTint,
        iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
        message = message,
        onDismissClick = onDismissClick,
        actionConfig = actionConfig
    )
}

@Composable
internal fun UserMessageErrorSnackbarM3(message: String, onDismissClick: () -> Unit, actionConfig: UserMessageSnackbarActionConfig? = null) {
    UserMessageSnackbarM3(
        iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_error),
        iconTint = MaterialTheme.colorScheme.error,
        message = message,
        onDismissClick = onDismissClick,
        actionConfig = actionConfig,
    )
}

@DayModePreview
@NightModePreview
@Composable
fun UserMessageInfoSnackbarPreviewLongText() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageSnackbarM3(
            iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
            message = "very very very very very very very very very very long info",
            actionConfig = UserMessageSnackbarActionConfig(TextRef.StringResource(R.string.kaleyra_user_message_pin), R.drawable.ic_kaleyra_stream_pin, {}),
            onDismissClick = {},
            backgroundColor = MaterialTheme.colorScheme.inverseSurface)
    }
}


@DayModePreview
@NightModePreview
@Composable
fun UserMessageInfoSnackbarPreviewShortText() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageSnackbarM3(
            iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
            message = "info",
            actionConfig = UserMessageSnackbarActionConfig(TextRef.StringResource(R.string.kaleyra_user_message_pin), R.drawable.ic_kaleyra_stream_pin, {}),
            onDismissClick = {},
            backgroundColor = MaterialTheme.colorScheme.inverseSurface)
    }
}


@DayModePreview
@NightModePreview
@Composable
fun UserMessageInfoSnackbarPreviewWithLongText() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageSnackbarM3(
            iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
            message = "Info with very very very very very very very very very very long text",
            onDismissClick = {},
            backgroundColor = MaterialTheme.colorScheme.inverseSurface)
    }
}

@DayModePreview
@NightModePreview
@Composable
fun UserMessageErrorSnackbarPreview() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageErrorSnackbarM3(
            message = "Info with short text",
            onDismissClick = {}
        )
    }
}

@DayModePreview
@NightModePreview
@Composable
fun AlertMessageSnackbarPreview() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageInfoSnackbarM3(
            message = "Alert text",
        )
    }
}

@DayModePreview
@NightModePreview
@Composable
fun OnlyTextMessagePreview() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageSnackbarM3(
            message = "only text",
        )
    }
}

@Composable
@MultiConfigPreview
fun NewFileDownloadSnackbarPreview() = KaleyraTheme {
    NewSignatureSnackbar {}
}
