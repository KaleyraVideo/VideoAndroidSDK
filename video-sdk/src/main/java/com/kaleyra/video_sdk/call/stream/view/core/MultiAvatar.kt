package com.kaleyra.video_sdk.call.stream.view.core

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.extensions.ModifierExtensions.drawCircleBorder
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun MultiAvatar(
    userInfos: ImmutableList<UserInfo>,
    avatarSize: Dp,
    avatarCount: Int,
    borderColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val spacing = remember(avatarSize) { - avatarSize * .25f }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarToDisplayCount = if (userInfos.count() > avatarCount) avatarCount - 1 else avatarCount
            userInfos.value
                .take(avatarToDisplayCount)
                .fastForEach { (id: String, username: String, uri: ImmutableUri) ->
                    key(id) {
                        Avatar(
                            username = username,
                            uri = uri,
                            size = avatarSize,
                            placeholder =  R.drawable.ic_kaleyra_avatar,
                            borderWidth = borderWidth,
                            borderColor = borderColor,
                            backgroundColor = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

            if (avatarToDisplayCount < avatarCount) {
                val overflowCount = userInfos.value.drop(avatarToDisplayCount).size
                key("overflowId") {
                    Box(contentAlignment = Alignment.Center) {
                        val fontSize = with(LocalDensity.current) { avatarSize.toSp() / 2.5 }
                        Box(
                            modifier = Modifier
                                .drawCircleBorder(borderColor, borderWidth)
                                .clip(CircleShape)
                                .size(avatarSize)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(R.string.kaleyra_users_avatars_overflow, overflowCount, overflowCount),
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = fontSize,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun StreamAvatarPreview() {
    KaleyraTheme {
        Surface {
            MultiAvatar(
                userInfos = listOf(
                    UserInfo("userId1", "John", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId2", "Mario", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                ).toImmutableList(),
                avatarSize = 40.dp,
                avatarCount = 4,
            )
        }
    }
}