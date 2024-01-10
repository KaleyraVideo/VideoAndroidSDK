package com.kaleyra.video_sdk.call.participantspanel.view

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.horizontalCutoutPadding
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
fun ParticipantsPanelDetailComponent(
    streamUi: StreamUi,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar_bold,
    @DrawableRes avatarError: Int = R.drawable.ic_kaleyra_avatar_bold,
    avatarSize: Dp = 56.dp,
    adminUserId: String? = null,
    isLoggedUserAdmin: Boolean,
    onMuteClicked: (Boolean) -> Unit,
    onPinClicked: (Boolean) -> Unit,
    onMoreClicked: () -> Unit,
) {

    val isMuted by remember { mutableStateOf(!(streamUi.audio?.isEnabled ?: false)) }
    val isPinned by remember { mutableStateOf(streamUi.pinned) }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalCutoutPadding()
            .background(color = if (isSystemInDarkTheme) Color(0xFF242424) else Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            uri = streamUi.avatar,
            contentDescription = stringResource(id = R.string.kaleyra_avatar),
            placeholder = avatarPlaceholder,
            error = avatarError,
            contentColor = LocalContentColor.current,
            backgroundColor = Color.LightGray,
            size = avatarSize
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier,
                lineHeight = 22.sp,
                text = streamUi.username,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                modifier = Modifier,
                lineHeight = 20.sp,
                fontSize = 13.22.sp,
                text = when {
                    streamUi.video?.isScreenShare == true -> stringResource(id = R.string.kaleyra_call_action_screen_share)
                    streamUi.username == adminUserId -> stringResource(id = R.string.kaleyra_admin_user)
                    else -> pluralStringResource(id = R.plurals.kaleyra_participants, count = 1)
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier,
            onClick = { onMuteClicked(!isMuted) },
        ) {
            Icon(
                painter = painterResource(
                    id = if (isMuted) R.drawable.ic_kaleyra_mic_off else R.drawable.kaleyra_z_mic_on
                ),
                contentDescription = stringResource(
                    id = if (isMuted) R.string.kaleyra_call_action_enable_mic_description else R.string.kaleyra_call_action_disable_mic_description
                ),
                tint = if (isSystemInDarkTheme) Color.White else Color.Black,
                modifier = Modifier
                    .size(24.dp)
            )
        }
        if (isLoggedUserAdmin) {
            IconButton(
                modifier = Modifier,
                onClick = onMoreClicked,
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_kaleyra_more
                    ),
                    contentDescription = stringResource(
                        id = R.string.kaleyra_call_action_more_description
                    ),
                    tint = if (isSystemInDarkTheme) Color.White else Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        } else {
            IconButton(
                modifier = Modifier,
                onClick = { onPinClicked(!isPinned) },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPinned) R.drawable.ic_kaleyra_unpin else R.drawable.ic_kaleyra_pin
                    ),
                    contentDescription = stringResource(
                        id = if (isPinned) R.string.kaleyra_call_action_unpin_description else R.string.kaleyra_call_action_pin_description
                    ),
                    tint = if (isSystemInDarkTheme) Color.White else Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                )
            }

        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantsPanelDetailPreview() = KaleyraTheme {
    ParticipantsPanelDetailComponent(
        streamUiMock.copy(pinned = true),
        isLoggedUserAdmin = false,
        adminUserId = "username",
        onMuteClicked = {},
        onPinClicked = {},
        onMoreClicked = {}
    )
}