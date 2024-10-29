@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Stable
data class UserMessageSnackbarActionConfig(val action: TextRef, val iconResource: Int? = null, val onActionClick: () -> Unit)

@Composable
internal fun UserMessageSnackbarM3(
    modifier: Modifier = Modifier,
    iconPainter: Painter,
    iconTint: Color = MaterialTheme.colorScheme.surface,
    message: String,
    actionConfig: UserMessageSnackbarActionConfig? = null,
    onDismissClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.surface
) {
    var isMultilineMessage by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Row(modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = if (actionConfig != null || isMultilineMessage) RoundedCornerShape(16.dp) else CircleShape
            )
            .align(Alignment.Center)) {
            if (actionConfig == null) {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                    painter = iconPainter,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Text(
                color = contentColor,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 4.dp, start = if (actionConfig != null) 16.dp else 8.dp, end = if (onDismissClick == null && actionConfig == null) 16.dp else 8.dp)
                    .align(Alignment.CenterVertically),
                text = message,
                style = MaterialTheme.typography.bodySmall,
                onTextLayout = { textLayoutResult ->
                    isMultilineMessage = textLayoutResult.lineCount > 1
                })
            if (actionConfig == null && onDismissClick != null) IconButton(
                modifier = Modifier
                    .padding(end = 16.dp, start = 8.dp)
                    .align(Alignment.CenterVertically)
                    .size(24.dp),
                onClick = onDismissClick) {
                Icon(
                    painter = painterResource(id = R.drawable.kaleyra_f_close),
                    tint = contentColor,
                    contentDescription = stringResource(id = R.string.kaleyra_close))
            }
            if (actionConfig !== null) FilledTonalButton(
                modifier = Modifier
                    .height(40.dp)
                    .padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                    .align(Alignment.CenterVertically),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = contentColor),
                onClick = actionConfig.onActionClick) {
                val actionDescription = actionConfig.action.resolve(LocalContext.current)
                if (actionConfig.iconResource != null) {
                    Icon(
                        tint = backgroundColor,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        painter = painterResource(id = actionConfig.iconResource),
                        contentDescription = actionDescription)
                }
                Text(
                    text = actionDescription,
                    color = backgroundColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
internal fun UserMessageInfoSnackbarM3(
    message: String, onDismissClick: (() -> Unit)? = null,
    actionConfig: UserMessageSnackbarActionConfig? = null) {
    UserMessageSnackbarM3(
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
fun UserMessageInfoSnackbarPreview() = KaleyraTheme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageSnackbarM3(
            iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
            message = "Info!!!",
            actionConfig = UserMessageSnackbarActionConfig(TextRef.StringResource(R.string.kaleyra_participants_component_pin), R.drawable.ic_kaleyra_stream_pin, {}),
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
            message = "Info with very very very very very very very very very very very very very very very very very very very very very very long text",
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