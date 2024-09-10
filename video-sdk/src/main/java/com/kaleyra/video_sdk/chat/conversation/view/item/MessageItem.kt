/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.chat.conversation.view.item

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.formatter.SymbolAnnotationType
import com.kaleyra.video_sdk.chat.conversation.formatter.messageFormatter
import com.kaleyra.video_sdk.chat.conversation.model.Message
import com.kaleyra.video_sdk.chat.conversation.view.MessageStateTag
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.flow.MutableStateFlow

internal val MessageItemAvatarSize = 28.dp
internal val OtherBubbleAvatarSpacing = 8.dp
internal val OtherBubbleLeftSpacing = 36.dp
internal val BubbleCornerRadius = 8.dp
internal val BubbleNoCornerRadius = 0.dp
internal val BubbleInBetweenPaddingSmall = 3.dp
internal val BubbleInBetweenPaddingMedium = 8.dp
internal const val BubbleTestTag = "BubbleTestTag"

@Composable
internal fun OtherMessageItem(
    message: Message.OtherMessage,
    isFirstChainMessage: Boolean = true,
    isLastChainMessage: Boolean = true,
    participantDetails: ChatParticipantDetails? = null,
    modifier: Modifier = Modifier
) {
    MessageRow(
        isFirstChainMessage = isFirstChainMessage,
        isLastChainMessage = isLastChainMessage,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            val uri = participantDetails?.image
            when {
                isLastChainMessage && uri != null -> {
                    Avatar(
                        uri = uri,
                        contentDescription = stringResource(id = R.string.kaleyra_avatar),
                        placeholder = R.drawable.ic_kaleyra_avatar,
                        error = R.drawable.ic_kaleyra_avatar,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        backgroundColor = colorResource(R.color.kaleyra_color_grey_light),
                        size = MessageItemAvatarSize
                    )
                    Spacer(modifier = Modifier.width(OtherBubbleAvatarSpacing))
                }

                uri != null -> Spacer(modifier = Modifier.width(OtherBubbleLeftSpacing))
            }
            Bubble(
                messageText = message.content,
                messageTime = message.time,
                username = if (isFirstChainMessage) participantDetails?.username else null,
                messageState = null,
                shape = RoundedCornerShape(
                    topStart = BubbleCornerRadius,
                    topEnd = BubbleCornerRadius,
                    bottomStart = if (isLastChainMessage) BubbleNoCornerRadius else BubbleCornerRadius,
                    bottomEnd = BubbleCornerRadius
                ),
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun MyMessageItem(
    message: Message.MyMessage,
    isFirstChainMessage: Boolean = true,
    isLastChainMessage: Boolean = true,
    modifier: Modifier = Modifier
) {
    val messageState by message.state.collectAsStateWithLifecycle(initialValue = Message.State.Read)
    MessageRow(
        isFirstChainMessage = isFirstChainMessage,
        isLastChainMessage = isLastChainMessage,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Bubble(
            messageText = message.content,
            messageTime = message.time,
            username = null,
            messageState = messageState,
            shape = RoundedCornerShape(
                topStart = BubbleCornerRadius,
                topEnd = BubbleCornerRadius,
                bottomStart = BubbleCornerRadius,
                bottomEnd = if (isLastChainMessage) BubbleNoCornerRadius else BubbleCornerRadius
            ),
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
internal fun MessageRow(
    isLastChainMessage: Boolean,
    isFirstChainMessage: Boolean,
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .padding(
                bottom = if (isLastChainMessage) BubbleInBetweenPaddingMedium else BubbleInBetweenPaddingSmall,
                top = if (isFirstChainMessage) BubbleInBetweenPaddingMedium else BubbleInBetweenPaddingSmall
            )
            .then(modifier),
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}

@Composable
internal fun Bubble(
    messageText: String,
    messageTime: String,
    username: String?,
    messageState: Message.State?,
    shape: Shape,
    backgroundColor: Color,
    contentColor: Color,
) {
    val configuration = LocalConfiguration.current

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        modifier = Modifier
            .widthIn(min = 0.dp, max = configuration.screenWidthDp.div(3).times(2).dp)
            .testTag(BubbleTestTag)
    ) {
        Column(modifier = Modifier.padding(16.dp, 8.dp)) {
            if (username != null) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            ClickableMessageText(
                messageText = messageText,
                textColor = contentColor
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = messageTime,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (messageState != null) {
                    Icon(
                        painter = painterFor(messageState),
                        contentDescription = contentDescriptionFor(messageState),
                        modifier = Modifier
                            .padding(2.dp)
                            .size(16.dp)
                            .testTag(MessageStateTag)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ClickableMessageText(messageText: String, textColor: Color) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(text = messageText, textColor = textColor)

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
        onClick = {
            styledMessage
                .getStringAnnotations(start = it, end = it)
                .firstOrNull()
                ?.let { annotation ->
                    when (annotation.tag) {
                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                        else -> Unit
                    }
                }
        }
    )
}

@Composable
private fun painterFor(state: Message.State): Painter =
    painterResource(
        id = when (state) {
            is Message.State.Sending -> R.drawable.ic_kaleyra_clock
            is Message.State.Sent -> R.drawable.ic_kaleyra_single_tick
            else -> R.drawable.ic_kaleyra_double_tick
        }
    )

@Composable
private fun contentDescriptionFor(state: Message.State): String =
    stringResource(
        id = when (state) {
            is Message.State.Sending -> R.string.kaleyra_chat_msg_status_pending
            is Message.State.Sent -> R.string.kaleyra_chat_msg_status_sent
            else -> R.string.kaleyra_chat_msg_status_seen
        }
    )

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun OtherMessageItemPreview() = KaleyraTheme {
    Surface {
        OtherMessageItem(
            message = Message.OtherMessage(
                "userId8",
                "id8",
                "Ut enim ad https://google.com minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "15:01"
            )
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun MyMessageItemPreview() = KaleyraTheme {
    Surface {
        MyMessageItem(
            message = Message.MyMessage(
                "id8",
                "Ut enim https://google.com ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "15:01",
                MutableStateFlow(Message.State.Read)
            )
        )
    }
}
