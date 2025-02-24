package com.kaleyra.video_sdk.call.stream.view.core

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
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

internal val MaxStreamAvatarSize = 96.dp
internal val MinStreamAvatarSize = 28.dp

@Composable
internal fun StreamAvatar(
    userInfos: ImmutableList<UserInfo>,
    avatarCount: Int,
    avatarSize: Dp? = null,
    borderColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderWidth: Dp = 0.dp,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val size = avatarSize ?: computeStreamAvatarSize(MaxStreamAvatarSize, MinStreamAvatarSize)
        val spacing = - size * .25f

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
                            size = size,
                            placeholder = avatarPlaceholder,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.drawCircleBorder(borderColor, borderWidth)
                        )
                    }
                }

            if (avatarToDisplayCount < avatarCount) {
                val overflowCount = userInfos.value.drop(avatarToDisplayCount).size
                key("overflowId") {
                    Box(contentAlignment = Alignment.Center) {
                        val fontSize = with(LocalDensity.current) { size.toSp() / 2 }
                        Box(
                            modifier = Modifier
                                .drawCircleBorder(borderColor, borderWidth)
                                .clip(CircleShape)
                                .size(size)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(R.string.kaleyra_users_avatars_overflow, overflowCount, overflowCount),
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = fontSize,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                        )
                    }
                }
            }

        }
    }
}

private fun BoxWithConstraintsScope.computeStreamAvatarSize(maxAvatarSize: Dp, minAvatarSize: Dp): Dp {
    val min = min(maxWidth, maxHeight)
    return (min / 2).coerceIn(minAvatarSize, maxAvatarSize)
}

@DayModePreview
@NightModePreview
@Composable
internal fun StreamAvatarPreview() {
    KaleyraTheme {
        Surface {
            StreamAvatar(
                avatarCount = 4,
                userInfos = listOf(
                    UserInfo("userId1", "John", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId2", "Mario", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                    UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
                ).toImmutableList(),
            )
        }
    }
}