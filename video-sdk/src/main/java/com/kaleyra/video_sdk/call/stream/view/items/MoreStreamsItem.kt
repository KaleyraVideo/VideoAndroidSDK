package com.kaleyra.video_sdk.call.stream.view.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

private const val AvatarCount = 3

private val MinAvatarSize = 36.dp
private val MaxAvatarSize = 48.dp

private val AvatarSpacing = (-16).dp

internal val MoreStreamsItemTag = "MoreStreamsItemTag"

@Composable
internal fun MoreStreamsItem(
    moreStreamsItem: StreamItem.MoreStreams,
    modifier: Modifier = Modifier,
) {
    val count = remember(moreStreamsItem) { moreStreamsItem.users.count() }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .testTag(MoreStreamsItemTag)
        ) {
            Avatars(moreStreamsItem)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.kaleyra_stream_other_participants, count),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium.copy(lineHeight = 24.sp),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun Avatars(moreStreamsItem: StreamItem.MoreStreams) {
    val color = MaterialTheme.colorScheme.surfaceContainerLow
    BoxWithConstraints {
        val avatarSize = (maxWidth / AvatarCount).coerceIn(MinAvatarSize, MaxAvatarSize)

        Row(horizontalArrangement = Arrangement.spacedBy(AvatarSpacing)) {
            moreStreamsItem.users
                .take(AvatarCount)
                .fastForEach { (id: String, username: String, uri: ImmutableUri?) ->
                    key(id) {
                        Avatar(
                            username = username,
                            uri = uri,
                            size = avatarSize,
                            modifier = Modifier
                                // Using drawWithContent modifier to get better result than using border modifier
                                .drawWithContent {
                                    drawCircle(
                                        color,
                                        blendMode = BlendMode.SrcIn,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                    drawContent()
                                }
                        )
                    }
                }
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
                    users = listOf(
                        MoreStreamsUserPreview(id = "1", username = "Viola J. Allen", null),
                        MoreStreamsUserPreview(id = "2", username = "John Doe", null),
                        MoreStreamsUserPreview(id = "3", username = "Mary Smith", null),
                    )
                )
            )
        }
    }
}
