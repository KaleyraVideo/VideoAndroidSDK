package com.kaleyra.video_sdk.call.appbar

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.common.button.BackIconButton
import com.kaleyra.video_sdk.extensions.ModifierExtensions.pulse
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import com.kaleyra.video_sdk.theme.typography

internal val CallAppBarHeight = 40.dp
internal val RecordingDotTag = "RecordingDotTag"

@Composable
internal fun CallAppBar(
    modifier: Modifier = Modifier,
    title: String,
    logo: Logo,
    recording: Boolean,
    participantCount: Int,
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    Surface(
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier
            .fillMaxWidth()
            .height(CallAppBarHeight)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            content = { Title(recording = recording, title = title) }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackIconButton(
                    modifier = Modifier.size(32.dp),
                    icon = painterResource(id = R.drawable.ic_kaleyra_arrow_back_new),
                    iconTint = LocalContentColor.current,
                    onClick = onBackPressed
                )
                AsyncImage(
                    model = logo.let { if (!isDarkTheme) it.light else it.dark },
                    contentDescription = stringResource(id = R.string.kaleyra_company_logo),
                    contentScale = ContentScale.Fit,
                    modifier = modifier
                        .clip(CircleShape)
                        .size(22.dp)
                )
            }

            CallParticipantsButton(
                participantCount = participantCount,
                onClick = onParticipantClick
            )
        }
    }
}

@Composable
private fun Title(recording: Boolean, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(visible = recording) {
            Icon(
                painterResource(id = R.drawable.ic_kaleyra_recording_dot_new),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(18.dp)
                    .pulse()
                    .testTag(RecordingDotTag)
            )
        }
        Text(
            text = title,
            style = typography.titleSmall.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun CallParticipantsButton(
    participantCount: Int,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "$participantCount",
                style = typography.titleSmall.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_new),
                contentDescription = stringResource(id = R.string.kaleyra_show_participants_descr),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallInfoBarPreview() = KaleyraM3Theme {
    CallAppBar(
        logo = Logo(light = Uri.EMPTY, dark = Uri.EMPTY),
        title = "09:56",
        participantCount = 2,
        recording = true,
        onParticipantClick = {},
        onBackPressed = {}
    )
}