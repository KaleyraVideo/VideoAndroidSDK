package com.kaleyra.video_sdk.call.stream.view.items

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.view.core.MultiAvatar
import com.kaleyra.video_sdk.call.stream.view.core.computeStreamAvatarSize
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlin.math.roundToInt

private const val AvatarCount = 3

private val MinAvatarSize = 12.dp
private val MaxAvatarSize = 48.dp

internal val MoreStreamsItemTag = "MoreStreamsItemTag"

@Composable
internal fun MoreStreamsItem(
    moreStreamsItem: StreamItem.MoreStreams,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
    Surface(color = backgroundColor) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .testTag(MoreStreamsItemTag)
        ) {
            val avatarSize = remember(maxWidth, maxHeight) {
                computeStreamAvatarSize(
                    maxWidth,
                    maxHeight,
                    minAvatarSize = MinAvatarSize,
                    maxAvatarSize = MaxAvatarSize,
                    sizeRatio = AvatarCount
                )
            }
            val borderWidth = remember(avatarSize) { avatarSize * .05f }
            MultiAvatar(
                userInfos = moreStreamsItem.userInfos,
                avatarCount = AvatarCount,
                borderColor = backgroundColor,
                borderWidth = borderWidth,
                avatarSize = avatarSize
            )

            Text(
                text = stringResource(id = R.string.kaleyra_stream_other_participants),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .offset {
                        val yDp = (avatarSize / 2) + 16.dp
                        val y = yDp.toPx().roundToInt()
                        IntOffset(x = 0, y = y)
                    }
            )
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun MoreStreamsItemPreview() {
    KaleyraTheme {
        Surface {
            MoreStreamsItem(
                moreStreamsItem = StreamItem.MoreStreams(
                    userInfos = listOf(
                        UserInfo(userId = "1", username = "Viola J. Allen", ImmutableUri()),
                        UserInfo(userId = "2", username = "John Doe", ImmutableUri()),
                        UserInfo(userId = "3", username = "Mary Smith", ImmutableUri()),
                    ).toImmutableList()
                )
            )
        }
    }
}
