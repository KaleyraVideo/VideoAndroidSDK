package com.kaleyra.video_sdk.call.stream.view.core

import android.net.Uri
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun StreamPreview(
    streamView: ImmutableView<VideoStreamView>?,
    showStreamView: Boolean,
    userInfos: ImmutableList<UserInfo>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    avatarModifier: Modifier = Modifier
) {
    StreamLayout(
        modifier = modifier,
        streamView = streamView,
        showStreamView = showStreamView,
        onClick = onClick,
        avatar = {
            StreamAvatar(
                userInfos = userInfos,
                avatarCount = 3,
                modifier = avatarModifier
            )
        }
    )
}

@DayModePreview
@NightModePreview
@Composable
internal fun StreamPreviewPreview() {
    KaleyraTheme {
        Surface {
            StreamPreview(
                streamView = null,
                showStreamView = true,
                userInfos = listOf(
                    UserInfo("userId1", "John", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId2", "Mario", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                ).toImmutableList()
            )
        }
    }
}