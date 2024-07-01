@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.common.snackbarm3.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ColorFilter
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
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

private val UserMessageSnackBarMessageActionSpacing = 60.dp

@Stable
data class UserMessageSnackbarActionConfig(val action: TextRef, val onActionClick: () -> Unit)

@Composable
internal fun UserMessageSnackbarM3(
    iconPainter: Painter,
    message: String,
    actionConfig: UserMessageSnackbarActionConfig? = null,
    onDismissClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.surface
) {
    var dismissButtonSize by remember { mutableStateOf(IntSize.Zero) }
    var actionsSize by remember { mutableStateOf(IntSize.Zero) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
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
                shape = CircleShape,
                containerColor = backgroundColor,
                modifier = Modifier.padding(vertical = 8.dp),
                dismissAction = {
                    if (actionConfig != null) Unit
                    else if (onDismissClick == null) Unit
                    else IconButton(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates -> dismissButtonSize = IntSize(coordinates.size.width, 0) }
                            .padding(end = 8.dp),
                        onClick = onDismissClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.kaleyra_f_close),
                            tint = contentColor,
                            contentDescription = stringResource(id = R.string.kaleyra_close))
                    }
                },
                action = {
                    actionConfig ?: return@Snackbar
                    Row(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates -> actionsSize = IntSize(coordinates.size.width, 0) },
                        verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            onClick = actionConfig.onActionClick) {
                            Text(text = actionConfig.action.resolve(LocalContext.current))
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates -> contentSize = IntSize(coordinates.size.width, 0) }
                ) {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(contentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        color = contentColor,
                        modifier = Modifier.align(Alignment.CenterVertically).padding(end = if(onDismissClick == null && actionConfig == null) 8.dp else 0.dp),
                        text = message, fontWeight = FontWeight.Bold)
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
        message = message,
        onDismissClick = onDismissClick,
        actionConfig = actionConfig,
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    )
}

@MultiConfigPreview
@Composable
fun UserMessageInfoSnackbarPreview() = KaleyraM3Theme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageSnackbarM3(
            iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
            message = "Info!!!",
            actionConfig = UserMessageSnackbarActionConfig(TextRef.StringResource(R.string.kaleyra_participants_component_pin), {}),
            onDismissClick = {},
            backgroundColor = MaterialTheme.colorScheme.inverseSurface)
    }
}

@MultiConfigPreview
@Composable
fun UserMessageInfoSnackbarPreviewWithLongText() = KaleyraM3Theme {
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

@MultiConfigPreview
@Composable
fun UserMessageErrorSnackbarPreview() = KaleyraM3Theme {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center) {
        UserMessageErrorSnackbarM3(
            message = "Info with very very very very very very very very very very long text",
            onDismissClick = {}
        )
    }
}

@MultiConfigPreview
@Composable
fun AlertMessageSnackbarPreview() = KaleyraM3Theme {
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