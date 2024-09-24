@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

private val UserMessageSnackBarMessageActionSpacing = 30.dp

@Stable
data class UserMessageSnackbarActionConfig(val action: TextRef, val iconResource: Int? = null, val onActionClick: () -> Unit)

@Composable
internal fun UserMessageSnackbarM3(
    iconPainter: Painter,
    iconTint: Color = MaterialTheme.colorScheme.surface,
    message: String,
    actionConfig: UserMessageSnackbarActionConfig? = null,
    onDismissClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.surface
) {
    var dismissButtonSize by remember { mutableStateOf(IntSize.Zero) }
    var actionsSize by remember { mutableStateOf(IntSize.Zero) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    var messageSize by remember { mutableStateOf(IntSize.Zero) }
    var isMultilineMessage by remember { mutableStateOf(false) }
    val isPreview = LocalInspectionMode.current
    var displaySnackbar by remember { mutableStateOf(isPreview) }


    BoxWithConstraints(
        modifier = Modifier.alpha(if (displaySnackbar) 1f else 0f)
    ) {
        Row(modifier = Modifier.width(with(LocalDensity.current) {
            if (dismissButtonSize.width == 0 && actionsSize.width == 0 && contentSize.width == 0) {
                maxWidth
            } else {
                displaySnackbar = true
                (dismissButtonSize.width.toDp() + actionsSize.width.toDp() + contentSize.width.toDp() + UserMessageSnackBarMessageActionSpacing)
            }
        })) {
            Snackbar(
                shape = if (actionConfig != null || isMultilineMessage) RoundedCornerShape(16.dp) else CircleShape,
                containerColor = backgroundColor,
                dismissAction = {
                    if (actionConfig != null) Unit
                    else if (onDismissClick == null) Unit
                    else {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .onGloballyPositioned { coordinates -> dismissButtonSize = IntSize(coordinates.size.width, 0) }
                                    .padding(end = 16.dp, start = 8.dp)
                                    .size(24.dp),
                                onClick = onDismissClick) {
                                Icon(
                                    painter = painterResource(id = R.drawable.kaleyra_f_close),
                                    tint = contentColor,
                                    contentDescription = stringResource(id = R.string.kaleyra_close))
                            }
                        }
                    }
                },
                action = {
                    actionConfig ?: return@Snackbar
                    Row(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates -> actionsSize = IntSize(coordinates.size.width, 0) },
                        verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor =  contentColor),
                            modifier = Modifier
                                .height(32.dp)
                                .padding(end = 4.dp),
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
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates -> contentSize = IntSize(coordinates.size.width, 0) }
                    ) {
                        if (actionConfig == null) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = iconPainter,
                                contentDescription = null,
                                tint = iconTint
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            color = contentColor,
                            modifier = Modifier
                                .onGloballyPositioned { coordinates -> messageSize = IntSize(0, coordinates.size.height) }
                                .padding(end = if (onDismissClick == null && actionConfig == null) 4.dp else 0.dp),
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            onTextLayout = { textLayoutResult ->
                                isMultilineMessage = textLayoutResult.lineCount > 1
                            })
                    }
                }
            }
        }
    }
}

@Composable
internal fun UserMessageInfoSnackbarM3(message: String, onDismissClick: (() -> Unit)? = null, actionConfig: UserMessageSnackbarActionConfig? = null) {
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